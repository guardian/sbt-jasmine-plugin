package com.gu

import sbt._
import Keys._
import io.Source
import org.mozilla.javascript.{ScriptableObject, ContextFactory, Context, Function => JsFunction}
import org.mozilla.javascript.tools.shell.{Global, Main}
import java.io.{FileReader, InputStreamReader}


object SbtJasminePlugin extends Plugin {

  lazy val jasmineTestDir = SettingKey[Seq[File]]("jasmineTestDir", "Path to directory containing the /specs and /mocks directories")
  lazy val appJsDir = SettingKey[Seq[File]]("appJsDir", "the root directory where the application js files live")
  lazy val appJsLibDir = SettingKey[Seq[File]]("appJsLibDir", "the root directory where the application's js library files live")
  lazy val jasmineConfFile = SettingKey[Seq[File]]("jasmineConfFile", "the js file that loads your js context and configures jasmine")
  lazy val jasmine = TaskKey[Unit]("jasmine", "Run jasmine tests")

  def jasmineTask = (jasmineTestDir, appJsDir, appJsLibDir, jasmineConfFile, streams) map { (testJsRoots, appJsRoots, appJsLibRoots, confs, s) =>

    s.log.info("running jasmine...")

    val errorCounts = for {
        testRoot <- testJsRoots
        appJsRoot <- appJsRoots
        appJsLibRoot <- appJsLibRoots
        conf <- confs
    } yield {
      val jscontext =  new ContextFactory().enterContext()
      val scope = new Global()
      scope.init(jscontext)

      jscontext.evaluateReader(scope, bundledScript("sbtjasmine.js"), "sbtjasmine.js", 1, null)

      // js func = function runTests(appJsRoot, appJsLibRoot, testRoot, confFile)
      val runTestFunc = scope.get("runTests", scope).asInstanceOf[JsFunction]
      val errorsInfile = runTestFunc.call(jscontext, scope, scope, Array(
        appJsRoot.getAbsolutePath,
        appJsLibRoot.getAbsolutePath,
        testRoot.getAbsolutePath,
        conf.getAbsolutePath))
      errorsInfile.asInstanceOf[Double]
    }

    val errorCount = errorCounts.sum
    if (errorCount > 0) throw new JasmineFailedException(errorCount.toInt)
  }

  def bundledScript(fileName: String) = {
    val cl = this.getClass.getClassLoader
    val is = cl.getResourceAsStream(fileName)

    new InputStreamReader(is)
  }

  val jasmineSettings: Seq[Project.Setting[_]] = Seq(
    jasmine <<= jasmineTask,
    appJsDir := Seq(),
    appJsLibDir := Seq(),
    jasmineTestDir := Seq(),
    jasmineConfFile := Seq()
  )
}

class JasmineFailedException(count: Int) extends Exception("jasmine failed with " + count + " errors")

class BundledLibraryReaderFactory(resourcePath: String) {
  lazy val cl = this.getClass.getClassLoader
  def reader = new InputStreamReader(cl.getResourceAsStream(resourcePath))
}
