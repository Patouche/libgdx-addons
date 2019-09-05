dependencies {
    api(project(":core-addons"))
    implementation(kotlin("stdlib"))
    api(Dependencies.ashley)
    implementation(Dependencies.ktx_log)
    testImplementation(TestDependencies.junit)
    testImplementation(TestDependencies.assertJ)
    testImplementation(TestDependencies.mockito)
}
