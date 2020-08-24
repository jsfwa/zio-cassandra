ThisBuild / githubWorkflowJavaVersions := Seq("adopt@11")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.Equals(Ref.Branch("master")))

ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("compile")),
  WorkflowStep.Sbt(List("test")),
  WorkflowStep.Sbt(List("mimaReportBinaryIssues"))
)

ThisBuild / githubWorkflowEnv ++= Map(
  "SONATYPE_USERNAME" -> s"$${{ secrets.SONATYPE_USERNAME }}",
  "SONATYPE_PASSWORD" -> s"$${{ secrets.SONATYPE_PASSWORD }}",
  "PGP_SECRET" -> s"$${{ secrets.PGP_SECRET }}"
)

ThisBuild / githubWorkflowPublishPreamble +=
  WorkflowStep.Run(
    List("echo $PGP_SECRET | base64 -d | gpg --import"),
    name = Some("Import signing key")
  )