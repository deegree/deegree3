pipeline {
    agent any
    options {
        disableConcurrentBuilds()
    }
    tools {
        maven 'maven-3.9'
        jdk 'temurin-jdk17'
        git 'git-default'
        generic 'cyclonedx'
    }
    environment {
        MAVEN_OPTS='-Djava.awt.headless=true -Xmx4096m'
    }
    parameters {
          string name: 'REL_VERSION', defaultValue: "3.6.x", description: 'Next release version'
          string name: 'DEV_VERSION', defaultValue: "3.6.x-SNAPSHOT", description: 'Next snapshot version'
          booleanParam name: 'PERFORM_RELEASE', defaultValue: false, description: 'Perform release build (on main branch only)'
    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
                sh 'mvn -version'
                sh 'java -version'
                sh 'git --version'
                sh 'DOTNET_SYSTEM_GLOBALIZATION_INVARIANT=1 cyclonedx --version'
            }
        }
        stage ('Reduced Build for testing') {
            steps {
                echo 'Building'
                sh 'mvn -B -C clean package'
            }
        }
        stage ('SBOM Comparsion') {
            environment {
                MAVEN_HELP_GOAL='org.apache.maven.plugins:maven-help-plugin:3.5.1:evaluate'
                SBOM_LOCATION='target/bom.json'
                DOTNET_SYSTEM_GLOBALIZATION_INVARIANT='1'
                POM_VERSION="""${sh(
                    returnStdout: true,
                    script: 'mvn ${MAVEN_HELP_GOAL} -Dexpression=project.version -q -DforceStdout'
                ).trim()}"""
                TAG_VERSION="""${sh(
                    returnStdout: true,
                    script: '(git describe --abbrev=0 --tags --match "deegree-*.*" 2>/dev/null || echo "deegree-0.0.0") | cut -d"-" -f 2-'
                ).trim()}"""
            }
            steps {
                sh 'mvn -U dependency:copy -Dmdep.stripVersion=true -DoutputDirectory=target/sbom-snapshot -Dartifact=org.deegree:deegree:${POM_VERSION}:json:cyclonedx || true'
                sh 'mvn -U dependency:copy -Dmdep.stripVersion=true -DoutputDirectory=target/sbom-last-tag -Dartifact=org.deegree:deegree:${TAG_VERSION}:json:cyclonedx || true'
                echo 'Compare last repository snapshot bom with current build'
                sh 'cyclonedx diff --output-format text --component-versions target/sbom-snapshot/deegree-cyclonedx.json ${SBOM_LOCATION} || true'
                echo 'Compare last repository tag bom with current build'
                sh 'cyclonedx diff --output-format text --component-versions target/sbom-last-tag/deegree-cyclonedx.json ${SBOM_LOCATION} || true'
            }
        }
        stage ('Release') {
            when {
                allOf {
                    triggeredBy cause: "UserIdCause", detail: "tmc"
                    expression { return params.PERFORM_RELEASE }
                }
            }
            steps {
                echo 'Prepare release version ${REL_VERSION}'
                withMaven(mavenSettingsConfig: 'mvn-server-settings', options: [junitPublisher(healthScaleFactor: 1.0)], publisherStrategy: 'EXPLICIT') {
                  withCredentials([usernamePassword(credentialsId:'nexus-deploy', passwordVariable: 'PASSWORD_VAR', usernameVariable: 'USERNAME_VAR')]) {
                    sshagent(credentials: ['jenkins-deegree-ssh-key']) {
                      sh 'mvn release:clean release:prepare -P integration-tests,oracle,handbook -Dresume=false -DreleaseVersion=${REL_VERSION} -DdevelopmentVersion=${DEV_VERSION}'
                      sh 'mvn release:perform -P integration-tests,oracle,handbook -DdeployAtEnd=true -Dgoals=deploy -Drepo.username=${USERNAME_VAR} -Drepo.password=${PASSWORD_VAR}'
                    }
                  }
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/deegree-webservices-*.war', fingerprint: true
                    archiveArtifacts artifacts: '**/target/deegree-documentation*.zip', fingerprint: true
                }
            }
        }
    }
    post {
        always {
            cleanWs notFailBuild: true
        }
    }
}