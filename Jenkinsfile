node {
   def mvnHome
   stage('下準備') {
      git 'https://github.com/ryuuta0217/JMusicBot-JP.git'         
      mvnHome = tool 'M3'
   }
   stage('ビルド') {
      withEnv(["MVN_HOME=$mvnHome"]) {
         if (isUnix()) {
            sh '"$MVN_HOME/bin/mvn" -Dmaven.test.failure.ignore -Dfile.encoding=UTF-8 clean package'
         } else {
            bat(/"%MVN_HOME%\bin\mvn" -Dmaven.test.failure.ignore -Dfile.encoding=UTF-8 clean package/)
         }
      }
   }
   stage('結果') {
      junit '**/target/surefire-reports/TEST-*.xml'
      archiveArtifacts 'target/*.jar'
   }
}
