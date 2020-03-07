pipeline {
  agent none
  stages {
    stage('Build') {
      steps {
        sh '$MVN_HOME/bin/mvn -B -DskipTests clean package'
      }
    }

    stage('Test') {
      post {
        always {
          junit 'target/surefire-reports/*.xml'
        }

      }
      steps {
        sh '$MVN_HOME/bin/mvn test'
      }
    }

    stage('Deliver') {
      steps {
        sh './jenkins/scripts/deliver.sh'
      }
    }

  }
  options {
    skipStagesAfterUnstable()
  }
}