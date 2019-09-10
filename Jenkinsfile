
pipeline {
   agent any
   def mvnHome
   
   stages {
      stage('Prepare') {
         steps {
            git 'https://github.com/ryuuta0217/JMusicBot-JP.git'         
            mvnHome = tool 'Maven 3.6.0'
         }
      }
      
      stage('build') {
         steps {
            withEnv(["MVN_HOME=$mvnHome"]) {
               if(isUnix()) {
                  sh '"$MVN_HOME/bin/mvn" -Dmaven.test.failure.ignore -Dfile.encoding=UTF-8 clean package'
               } else {
                  bat(/"%MVN_HOME%\bin\mvn" -Dmaven.test.failure.ignore -Dfile.encoding=UTF-8 clean package/)
               }
            }
         }
      }
      
      stage('result') {
         steps {
            junit '**/target/surefire-reports/TEST-*.xml'
         }
      }
   }
}
