pipeline {
    agent any
    options {
        disableConcurrentBuilds()
    }
    tools {
        maven 'maven-3.9'
        jdk 'temurin-jdk17'
        git 'git-default'
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
                sh 'mvn -B -C -fae -P oracle com.github.spotbugs:spotbugs-maven-plugin:4.9.7.0:spotbugs javadoc:javadoc'
            }
            post {
                always {
                    recordIssues enabledForFailure: true, tools: [mavenConsole(), java(), javaDoc(), spotBugs()]
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