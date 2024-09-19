pipeline {
    agent any

    stages {
        stage('backup&gitpull') {
        parallel{   
             stage('backup'){
             steps{
             sh 'mv /var/www/html/* /nginx'   
             }
             }
            stage('gitpull'){
            steps {
            sh 'cd /nginx/nginx_cicd/ && git pull'
            }   } }
        }
        stage('deploymebnt'){
            steps{
            sh 'cp -r /nginx/nginx_cicd/* /var/www/html'
            }
        }
        stage('testing'){
            steps{
            sh 'curl localhost:80'
            }
        }
        stage('Roolback'){
            steps{
            catchError(buildResult: 'ABORTED', message: 'ABORTED?', stageResult: 'ABORTED') {
            input message: 'SURE ROLLBACK!', ok: 'SURE ROLLBACK!', parameters: [booleanParam('')]
            sh 'mv /nginx/index.html /var/www/html'

            }   
            }
        }
        stage('production-stage'){
            steps{
                 script{
                    def remote = [:]
                    remote.name = 'production-start'
                    remote.host = '192.168.1.12'
                    remote.user = 'pro'
                    remote.password = '123'
                    remote.allowAnyHosts = true
                    stage('backup') {
                    catchError(buildResult: 'ABORTED', message: 'skip-step?', stageResult: 'FAILURE'){
                    sshCommand remote: remote, command: "mv /var/www/html/* /nginx/"
                    }
                    }
                    stage("gitpull"){
                    sshCommand remote: remote, command: "cd /nginx/nginx_cicd && git pull"
                    }
                    stage('production'){
                    sshCommand remote: remote, command: "cp -r /nginx/nginx_cicd/* /var/www/html"
                    }
                    stage('testing'){
                    sshCommand remote: remote, command: "curl localhost:80"
                    }
                    stage('Roolback'){
                      input message: 'SURE ROLLBACK!', ok: 'SURE ROLLBACK!', parameters: [booleanParam('')]
                      sshCommand remote: remote, command: "mv /var/www/html/* /nginx/nginx_bk && mv /nginx/index.html  /var/www/html/"
                    
                    }
  
                 }
            }
            
            

            
        }

        
    }
}
