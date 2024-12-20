package org.fluentjava.perftence.wsdef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.Concatenated.ConcatenatedBuilder;
import org.fluentjava.iwant.api.core.ScriptGenerated;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.api.wsdef.WorkspaceContext;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;
import org.fluentjava.iwant.eclipsesettings.EclipseSettings;
import org.fluentjava.iwant.plugin.jacoco.JacocoDistribution;
import org.fluentjava.iwant.plugin.jacoco.JacocoTargetsOfJavaModules;

public class PerftenceWorkspace implements Workspace {

	private final PerftenceModules modules;

	public PerftenceWorkspace(WorkspaceContext ctx) {
		this.modules = new PerftenceModules(ctx);
	}

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		List<Target> t = new ArrayList<>();
		t.add(classdirList());
		t.add(jacocoReportAll());
		Target perftenceDistribution = Distro.withModules(modules);
		t.add(perftenceDistribution);
		t.add(tarred(perftenceDistribution));
		return t;
	}

	// TODO use from iwant codebase when available
	private static Target tarred(Path dirToTar) {
		String tarName = dirToTar.name() + ".tar";
		ConcatenatedBuilder tarScript = Concatenated.named(tarName + ".sh");
		tarScript.string("#!/bin/bash\n");
		tarScript.string("set -eu\n");
		tarScript.string("DEST=$1\n");
		tarScript.string("SRC=").unixPathTo(dirToTar).string("\n");
		tarScript.string("SRCDIR=$(dirname \"$SRC\")\n");
		tarScript.string("SRCBASE=$(basename \"$SRC\")\n");
		tarScript.string("cd \"$SRCDIR\"\n");
		tarScript.string("tar cf \"$DEST\" \"$SRCBASE\"\n");
		return ScriptGenerated.named(tarName).byScript(tarScript.end());
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.modules(modules.allSrcModules()).end());
	}

	private Target jacocoReportAll() {
		return jacocoReport("jacoco-report-all", modules.modulesForJacoco());
	}

	private static Target jacocoReport(String name,
			SortedSet<JavaSrcModule> interestingModules) {
		return JacocoTargetsOfJavaModules.with().jacoco(jacoco())
				.antJars(TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar())
				.modules(interestingModules).end().jacocoReport(name);

	}

	private static JacocoDistribution jacoco() {
		return JacocoDistribution.newestTestedVersion();
	}

	private Target classdirList() {
		return classdirListOf("classdir-list", modules.allSrcModules(), true);
	}

	private static Target classdirListOf(String name,
			Collection<? extends JavaModule> modules, boolean includeTests) {
		ConcatenatedBuilder classdirs = Concatenated.named(name);
		for (JavaModule module : modules) {
			Path mainClasses = module.mainArtifact();
			if (mainClasses != null) {
				classdirs.unixPathTo(mainClasses).string("\n");
			}
			if (includeTests && module instanceof JavaSrcModule) {
				JavaSrcModule srcMod = (JavaSrcModule) module;
				Path testClasses = srcMod.testArtifact();
				if (testClasses != null) {
					classdirs.unixPathTo(testClasses).string("\n");
				}
			}
		}
		return classdirs.end();
	}

}
