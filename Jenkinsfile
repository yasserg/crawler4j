properties([
    pipelineTriggers([
        pollSCM('H/2 * * * *')
    ])
])

pipeline {
    agent none
    options { 
        skipDefaultCheckout()
    }
    stages {
        stage('Run checks') {
            agent {
                kubernetes {
                    label 'sandi-metz-enforcer-pod'
                    containerTemplate {
                        name 'sandi-metz-enforcer-container'
                        image 'registry2.swarm.devfactory.com/codenation/sandimetz-enforcer:v1.0.2'
                        ttyEnabled true
                        command 'cat'
                    }
                }
            }
            when {
                beforeAgent true
                expression { env.BRANCH_NAME ==~ /(PR-\d+)/ && env.CHANGE_TARGET ==~ /(master|develop)/ }
            }
            steps {
                container('sandi-metz-enforcer-container') {
                    script {
                        def repositoryUrl = scm.getUserRemoteConfigs()[0].getUrl()
                        echo "Validating rules on ${repositoryUrl}:${env.BRANCH_NAME}"
                        env.getEnvironment().each { name, value -> println "Name: $name -> Value $value" }
                        sh "REPO_URL=${repositoryUrl} BRANCH=${env.CHANGE_BRANCH} bash sandimetz.enforcer.sh"
                    }
                }
            }
        }
    }
}
