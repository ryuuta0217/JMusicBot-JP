node {
   def mvnHome
   stage('Prepare') {
      git 'https://github.com/ryuuta0217/JMusicBot-JP.git'         
      mvnHome = tool 'Maven 3.6.0'
   }
   stage('build') {
      withEnv(["MVN_HOME=$mvnHome"]) {
         if (isUnix()) {
            sh '"$MVN_HOME/bin/mvn" -Dmaven.test.failure.ignore -Dfile.encoding=UTF-8 clean package'
         } else {
            bat(/"%MVN_HOME%\bin\mvn" -Dmaven.test.failure.ignore -Dfile.encoding=UTF-8 clean package/)
         }
      }
   }
   stage('result') {
      junit '**/target/surefire-reports/TEST-*.xml'
      archiveArtifacts 'target/*.jar'
   }
}
