package org.fluentjava.perftence.wsdef;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.fluentjava.iwant.api.javamodules.CodeFormatterPolicy;
import org.fluentjava.iwant.api.javamodules.CodeFormatterPolicy.TabulationCharValue;
import org.fluentjava.iwant.api.javamodules.CodeStyle;
import org.fluentjava.iwant.api.javamodules.CodeStylePolicy;
import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaCompliance;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.wsdef.WorkspaceContext;
import org.fluentjava.iwant.core.download.Downloaded;
import org.fluentjava.iwant.core.javamodules.JavaModules;

public class PerftenceModules extends JavaModules {

	private static final CodeFormatterPolicy CODE_FORMATTER_POLICY = codeFormatterPolicy();
	private static final String VÖLUNDR_VERSION = "2.1.1";
	// bin
	private final Path afreechartJar = Downloaded.withName("afreechartJar").url(
			"https://github.com/gmarques33/repos/raw/master/releases/org/afree/afreechart/"
					+ "0.0.4/afreechart-0.0.4.jar")
			.md5("cdca9ce40b95c104f44e624dc6ee29c2");
	private final JavaModule afreechart = JavaBinModule.providing(afreechartJar)
			.end();

	final JavaBinModule asmAll = binModule("org.ow2.asm", "asm-all", "5.0.1");

	private final JavaModule commonsCollections = binModule(
			"commons-collections", "commons-collections", "3.2.1");

	private final JavaModule jcommon = srclessBinModule("jfree", "jcommon",
			"1.0.15");

	private final JavaModule jfreechart = srclessBinModule("jfree",
			"jfreechart", "1.0.12");

	private final JavaModule junit = binModule("junit", "junit", "4.10");

	private final JavaModule log4j = binModule("log4j", "log4j", "1.2.16");

	private final JavaModule slf4jApi = binModule("org.slf4j", "slf4j-api",
			"1.6.1", log4j);

	private final JavaModule slf4jLog4j12 = binModule("org.slf4j",
			"slf4j-log4j12", "1.6.1");

	private final JavaModule völundrBag = völundrBinModule(
			"stronglytyped-sortedbag");

	private final JavaModule völundrConcurrent = völundrBinModule("concurrent");
	private final JavaModule völundrFileutil = völundrBinModule("fileutil");
	private final JavaModule völundrLinereader = völundrBinModule("linereader");
	private final JavaModule völundrStringToBytes = völundrBinModule(
			"string-to-bytes");
	private final JavaModule völundrStatistics = völundrBinModule("statistics");

	private final Set<JavaModule> iwant5runnerMods;

	public PerftenceModules(WorkspaceContext ctx) {
		this.iwant5runnerMods = ctx.iwantPlugin().junit5runner()
				.withDependencies();
		// let's evaluate them all before proceeding:
		dependencyRoots();
	}

	private IwantSrcModuleSpex ourModule(String name) {
		final String prefix = "perftence-";
		String prefixedName = name.startsWith(prefix) ? name : prefix + name;
		return srcModule(prefixedName).locationUnderWsRoot(name);
	}

	// src

	private JavaSrcModule perftenceGraph() {
		return lazy(() -> ourModule("perftence-graph").noTestJava()
				.noTestResources().end());
	}

	private JavaSrcModule perftenceGraphAfreechart() {
		return lazy(() -> ourModule("perftence-graph-afreechart").noTestJava()
				.noTestResources()
				.mainDeps(afreechart, perftenceGraph(), slf4jApi).end());
	}

	private JavaSrcModule perftence() {
		return lazy(() -> ourModule("perftence")
				.mainDeps(commonsCollections, perftenceGraph(), völundrBag,
						völundrStatistics, slf4jApi)
				.testDeps(junit, slf4jLog4j12, log4j).end());
	}

	private JavaSrcModule perftenceGraphJfreechart() {
		return lazy(() -> ourModule("perftence-graph-jfreechart").noTestJava()
				.noTestResources()
				.mainDeps(jcommon, jfreechart, perftence(), perftenceGraph(),
						slf4jApi, völundrFileutil, völundrStringToBytes)
				.end());
	}

	private JavaSrcModule perftenceTestreportHtml() {
		return lazy(
				() -> ourModule("perftence-testreport-html").noMainResources()
						.noTestJava().noTestResources().mainDeps(perftence(),
								slf4jApi, völundrFileutil, völundrStringToBytes)
						.end());
	}

	private JavaSrcModule perftenceDefaulttestruntimereporterfactory() {
		return lazy(
				() -> ourModule("perftence-defaulttestruntimereporterfactory")
						.noTestResources()
						.mainDeps(perftence(), perftenceGraph(), slf4jApi)
						.testDeps(junit, perftenceGraphJfreechart(),
								perftenceTestreportHtml())
						.end());
	}

	private JavaSrcModule reporterfactoryDependenciesJfreechart() {
		return lazy(() -> ourModule("reporterfactory-dependencies-jfreechart")
				.noTestJava().noTestResources()
				.mainDeps(perftence(),
						perftenceDefaulttestruntimereporterfactory(),
						perftenceGraph(), perftenceGraphJfreechart(),
						perftenceTestreportHtml())
				.end());
	}

	private JavaSrcModule reporterfactoryDependenciesAfreechart() {
		return lazy(() -> ourModule("reporterfactory-dependencies-afreechart")
				.noTestJava().noTestResources()
				.mainDeps(perftence(),
						perftenceDefaulttestruntimereporterfactory(),
						perftenceGraph(), perftenceGraphAfreechart(),
						perftenceTestreportHtml(), slf4jApi, afreechart,
						völundrFileutil, völundrStringToBytes)
				.end());
	}

	private JavaSrcModule perftenceFluent() {
		return lazy(() -> ourModule("perftence-fluent").noTestResources()
				.mainDeps(perftence(), perftenceGraph(), slf4jApi,
						völundrConcurrent)
				.testDeps(junit, perftenceDefaulttestruntimereporterfactory(),
						perftenceGraphJfreechart(), perftenceTestreportHtml(),
						reporterfactoryDependenciesJfreechart())
				.end());
	}

	private JavaSrcModule perftenceAgents() {
		return lazy(() -> ourModule("perftence-agents").noTestResources()
				.mainDeps(perftence(),
						perftenceDefaulttestruntimereporterfactory(),
						perftenceGraph(), slf4jApi, völundrBag)
				.testDeps(jfreechart, junit, perftenceGraphJfreechart(),
						perftenceTestreportHtml(),
						reporterfactoryDependenciesJfreechart())
				.end());
	}

	private JavaSrcModule perftenceApi() {
		return lazy(() -> ourModule("perftence-api").noTestResources()
				.mainDeps(perftence(), perftenceAgents(), perftenceFluent(),
						perftenceGraph())
				.testDeps(junit, perftenceDefaulttestruntimereporterfactory(),
						perftenceGraphJfreechart(), perftenceTestreportHtml(),
						reporterfactoryDependenciesJfreechart())
				.end());
	}

	private JavaSrcModule distributedPerftenceApi() {
		return lazy(() -> ourModule("distributed-perftence-api").noTestJava()
				.noTestResources()
				.mainDeps(perftence(),
						perftenceDefaulttestruntimereporterfactory(),
						perftenceFluent(), perftenceGraph(),
						perftenceGraphJfreechart(), perftenceTestreportHtml(),
						reporterfactoryDependenciesJfreechart(), slf4jApi,
						völundrConcurrent)
				.end());
	}

	private JavaSrcModule defaultPerftenceApiFactory() {
		return lazy(() -> ourModule("default-perftence-api-factory")
				.noMainResources().noTestJava().noTestResources()
				.mainDeps(perftence(), perftenceApi(),
						perftenceDefaulttestruntimereporterfactory(),
						perftenceGraph(),
						reporterfactoryDependenciesJfreechart(),
						perftenceTestreportHtml(), völundrFileutil,
						völundrStringToBytes)
				.end());
	}

	private JavaSrcModule perftenceJunit() {
		return lazy(() -> ourModule("perftence-junit").noTestResources()
				.mainDeps(defaultPerftenceApiFactory(), junit, perftence(),
						perftenceAgents(), perftenceApi(), perftenceFluent(),
						slf4jApi)
				.end());
	}

	private JavaSrcModule acceptanceTests() {
		return lazy(() -> ourModule("acceptance-tests").noMainJava()
				.noMainResources().noTestResources()
				.testDeps(junit, perftence(), perftenceAgents(),
						perftenceFluent(), perftenceJunit(), perftenceGraph(),
						perftenceGraphJfreechart(), perftenceTestreportHtml(),
						slf4jApi)
				.testRuntimeDeps(slf4jLog4j12, log4j).end());
	}

	private JavaSrcModule mainentrypointExample() {
		return lazy(() -> ourModule("mainentrypoint-example").noTestJava()
				.noTestResources()
				.mainDeps(defaultPerftenceApiFactory(), log4j, perftence(),
						perftenceApi(),
						perftenceDefaulttestruntimereporterfactory(),
						perftenceFluent(), perftenceGraph(),
						perftenceTestreportHtml(),
						reporterfactoryDependenciesJfreechart(), slf4jApi)
				.mainRuntimeDeps(log4j, slf4jLog4j12).end());
	}

	private JavaSrcModule perftenceExperimental() {
		return lazy(() -> ourModule("perftence-experimental").noMainResources()
				.noTestResources()
				.mainDeps(distributedPerftenceApi(), perftence(),
						perftenceAgents(), perftenceFluent(), slf4jApi,
						völundrConcurrent)
				.testDeps(commonsCollections, junit,
						perftenceDefaulttestruntimereporterfactory(),
						perftenceGraph(), perftenceGraphJfreechart(),
						perftenceJunit(), perftenceTestreportHtml(),
						reporterfactoryDependenciesJfreechart())
				.testRuntimeDeps(slf4jLog4j12, log4j).end());
	}

	private JavaSrcModule responsecodeSummaryappender() {
		return lazy(() -> ourModule("responsecode-summaryappender")
				.noMainResources().noTestResources()
				.mainDeps(perftence(), völundrBag).testDeps(junit).end());
	}

	private JavaSrcModule filebasedReportingProto() {
		return lazy(() -> ourModule("filebased-reporting-proto")
				.noMainResources()
				.mainDeps(perftence(), perftenceAgents(),
						perftenceDefaulttestruntimereporterfactory(),
						perftenceGraph(), perftenceGraphJfreechart(),
						perftenceTestreportHtml(),
						reporterfactoryDependenciesJfreechart(), slf4jApi,
						völundrStringToBytes, völundrLinereader)
				.testDeps(junit, perftenceFluent(), perftenceJunit())
				.testRuntimeDeps(log4j, slf4jLog4j12).end());
	}

	private JavaSrcModule fluentBasedExample() {
		return lazy(
				() -> ourModule("fluent-based-example").noMainJava()
						.noMainResources().testDeps(junit, perftence(),
								perftenceFluent(), perftenceJunit(), slf4jApi)
						.end());
	}

	private JavaSrcModule agentBasedExample() {
		return lazy(
				() -> ourModule("agent-based-example").noMainJava()
						.noMainResources()
						.testDeps(junit, log4j, perftence(), perftenceAgents(),
								perftenceJunit(), slf4jApi, slf4jLog4j12)
						.end());
	}

	private static JavaBinModule völundrBinModule(String lib) {
		return JavaBinModule.providing(
				Source.underWsroot(
						"lib-repo/" + lib + "-" + VÖLUNDR_VERSION + ".jar"),
				Source.underWsroot("lib-repo/sources/" + lib + "-"
						+ VÖLUNDR_VERSION + "-sources.jar"))
				.end();
	}

	// override common settings, like code formatter and code style

	@Override
	protected IwantSrcModuleSpex commonSettings(IwantSrcModuleSpex m) {
		return super.commonSettings(m).encoding(Charset.forName("UTF-8"))
				.codeFormatter(CODE_FORMATTER_POLICY)
				.codeStyle(CodeStylePolicy.defaultsExcept()
						.warn(CodeStyle.MISSING_DEFAULT_CASE)
						.warn(CodeStyle.REDUNDANT_SUPERINTERFACE)
						.warn(CodeStyle.UNUSED_TYPE_PARAMETER)
						.warn(CodeStyle.UNQUALIFIED_FIELD_ACCESS)
						.warn(CodeStyle.UNNECESSARY_ELSE)
						.warn(CodeStyle.POTENTIALLY_UNCLOSED_CLOSEABLE)

						.end())
				.javaCompliance(JavaCompliance.JAVA_17)
				.testDeps(iwant5runnerMods);
	}

	private static CodeFormatterPolicy codeFormatterPolicy() {
		CodeFormatterPolicy codeFormatterPolicy = new CodeFormatterPolicy();
		codeFormatterPolicy.lineSplit = 120;
		codeFormatterPolicy.tabulationChar = TabulationCharValue.SPACE;
		return codeFormatterPolicy;
	}

	// collections
	List<JavaSrcModule> productionDependencyRoots() {
		return Arrays.asList(distributedPerftenceApi(), perftence(),
				perftenceAgents(), perftenceApi(),
				perftenceDefaulttestruntimereporterfactory(), perftenceFluent(),
				perftenceGraph(), perftenceGraphAfreechart(),
				perftenceGraphJfreechart(), perftenceJunit(),
				perftenceTestreportHtml(),
				reporterfactoryDependenciesAfreechart(),
				reporterfactoryDependenciesJfreechart(),
				responsecodeSummaryappender());
	}

	/**
	 * Basically this just prevents warnings about unused modules
	 */
	List<JavaSrcModule> dependencyRoots() {
		List<JavaSrcModule> roots = new ArrayList<>();
		roots.addAll(productionDependencyRoots());
		roots.add(mainentrypointExample());
		roots.add(perftenceExperimental());
		roots.add(acceptanceTests());
		roots.add(agentBasedExample());
		roots.add(fluentBasedExample());
		roots.add(filebasedReportingProto());
		return roots;
	}

	public SortedSet<JavaSrcModule> modulesForJacoco() {
		SortedSet<JavaSrcModule> modulesForJacoco = new TreeSet<>(
				allSrcModules());
		modulesForJacoco.removeIf(new Predicate<JavaSrcModule>() {
			@Override
			public boolean test(JavaSrcModule module) {
				return module.equals(perftenceExperimental())
						|| module.equals(filebasedReportingProto())
						|| module.equals(mainentrypointExample())
						|| module.equals(fluentBasedExample())
						|| module.equals(agentBasedExample())
						|| module.equals(distributedPerftenceApi());
			}
		});
		return modulesForJacoco;
	}

}
