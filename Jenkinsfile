pipeline {
    agent {
        label 'openjdk11bot'
    }
    options { 
        disableConcurrentBuilds() 
    }
    tools {
        maven 'maven-3.8'
        jdk 'adoptopenjdk-jdk11'
    }
    environment {
        MAVEN_OPTS='-Djava.awt.headless=true -Xmx4096m'
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
               echo 'Unit testing'
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
            when {
                branch 'main'
            }
            steps {
                echo 'Preparing test harness: TEAM Engine'
                echo 'Download and start TEAM Engine'
                echo 'Start SUT deegree webapp with test configuration'
                echo 'Run FAT'
            }
            post {
                success {
                    echo 'FAT passed successfully'
                }
            }
        }
        stage ('Release') {
            when {
                branch 'main'
            }
            steps {
                echo 'Prepare release version...'
                echo 'Build and publish documentation'
                sh 'mvn -pl :deegree-webservices-handbook -Phandbook install'
                echo 'Build docker image...'
            }
            post {
                success {
                    // post release on github
                    archiveArtifacts artifacts: '**/target/deegree-webservices-*.war', fingerprint: true
                }
            }
        }
        stage ('Deploy PROD') {
            when {
                branch 'main'
            }
            // install current release version on demo.deegree.org
            steps {
                echo 'Deploying to demo.deegree.org...'
                echo 'Running smoke tests...'
            }
        }
    }
    post {
        always {
            cleanWs notFailBuild: true
        }
    }
}
