pipeline {
    agent {
        label 'openjdk8bot'
    }

    tools {
        maven 'maven-3.6'
        jdk 'adoptopenjdk-jdk8'
    }
    environment {
        MAVEN_OPTS='-Djava.awt.headless=true -Xmx3096m'
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
               sh 'mvn -B -C -Poracle,mssql clean test-compile'
            }
        }
        stage ('Integration Test') {
            steps {
                echo 'Integration testing'
                sh 'mvn -B -C -Pintegration-tests,oracle,mssql install'
            }
            post {
                always {
                    junit '**/target/*-reports/*.xml'
                }
            }
        }
        stage ('Quality Checks') {
            when {
                branch 'master'
            }
            steps {
                echo 'Quality checking'
                sh 'mvn -B -C -fae -Poracle,mssql com.github.spotbugs:spotbugs-maven-plugin:spotbugs checkstyle:checkstyle javadoc:javadoc'
            }
            post {
                success {
                    findbugs canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', pattern: '**/spotbugsXml.xml', unHealthy: ''
                    checkstyle canComputeNew: false, canRunOnFailed: true, defaultEncoding: '', healthy: '', pattern: '**/checkstyle-result.xml', unHealthy: ''
                }
            }
        }
        stage ('Acceptance Test') {
            steps {
                echo 'Preparing test environment'
                echo 'Download SUT deegree workspace'
                echo 'Start SUT deegree webapp with test configuration'
                sh 'mvn -pl :deegree-acceptance-tests -Pacceptance-tests,integration-tests verify'
                echo 'Run FAT'
            }
            post {
                success {
                    echo 'FAT passed successfully'
                }
            }
        }
        stage ('Release') {
            steps {
                echo 'Prepare release version'
                echo 'Build and publish documentation'
                sh 'mvn -pl :deegree-webservices-handbook -Phandbook package'
                sh 'mvn site -Psite-all-reports'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/deegree-webservices-*.war,**/target/deegree-webservices-handbook-*.zip', fingerprint: true, followSymlinks: false, onlyIfSuccessful: true
                }
            }
        }
        stage ('Deploy') {
            steps {
                echo 'Deploying to maven repo'
                nexusArtifactUploader artifacts: [[artifactId: 'deegree-webservices', classifier: 'jdk11',
                    file: '**/deegree-webservices/target/deegree-webservices-3.4.14-SNAPSHOT-jdk11.war', type: 'war']],
                    credentialsId: '26707569-2c7c-4517-9a60-350b6c5041ca',
                    groupId: 'de.latlon.vfwcts',
                    nexusUrl: 'repo.lat-lon.de/repository/', nexusVersion: 'nexus3',
                    protocol: 'https', repository: 'vodafone-snapshots',
                    version: '4.0-SNAPSHOT'
            }
        }
    }
}
