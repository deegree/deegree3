pipeline {
    agent {
        label 'openjdk11bot'
    }
    options { 
        disableConcurrentBuilds()
    }
    tools {
        maven 'maven-3.9'
        jdk 'adoptopenjdk-jdk11'
    }
    environment {
        MAVEN_OPTS='-Djava.awt.headless=true -Xmx4096m'
    }
    parameters {
          string name: 'REL_VERSION', defaultValue: "3.5.x", description: 'Next release version'
          string name: 'DEV_VERSION', defaultValue: "3.5.x-SNAPSHOT", description: 'Next snapshot version'
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
            }
        }
        stage ('Build') {
            steps {
               echo 'Building'
               sh 'mvn -B -C -P oracle clean test-compile'
            }
        }
        stage ('Test') {
            steps {
                echo 'Testing'
                sh 'mvn -B -C -fae -P integration-tests,oracle install'
            }
            post {
                always {
                    junit '**/target/*-reports/*.xml'
                }
            }
        }
        stage ('Quality Checks') {
            when {
                branch 'main'
            }
            steps {
                echo 'Quality checking'
                sh 'mvn -B -C -fae -P oracle com.github.spotbugs:spotbugs-maven-plugin:spotbugs javadoc:javadoc'
            }
            post {
                always {
                    recordIssues enabledForFailure: true, tools: [mavenConsole(), java(), javaDoc()]
                    recordIssues enabledForFailure: true, tool: spotBugs()
                }
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
                sh 'mvn -Dresume=false -DreleaseVersion=${REL_VERSION} -DdevelopmentVersion=${DEV_VERSION} -DdeployAtEnd=true -Dgoals=deploy release:prepare release:perform -P integration-tests,oracle,handbook'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/deegree-webservices-*.war', fingerprint: true
                    archiveArtifacts artifacts: '**/target/deegree-webservices-handbook*.zip', fingerprint: true
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
