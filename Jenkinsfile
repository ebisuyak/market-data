def withPod(body) {
	podTemplate(label: 'pod', serviceAccount: 'jenkins', containers: [
		containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
		containerTemplate(name: 'kubectl', image: 'ebisuyak/kubectl:v1.18.2', command: 'cat', ttyEnabled: true)
	],
	volumes: [
		hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'),
	]
	) { body() }
}

withPod {
	node('pod') {
		def tag = "${env.BRANCH_NAME}.${env.BUILD_NUMBER}"
		def service = "market-data:${tag}"
		checkout scm
		container('docker') {
			stage('Build') {
				sh("docker build -t ${service} .")
			}
			stage('Test') {
				try {
					sh("docker run -v `pwd`:/workspace --rm ${service} python setup.py test")
				} finally {
					step([$class: 'JUnitResultArchiver', testResults: 'results.xml'])
				}
			}
			def tagToDeploy = "ebisuyak/${service}"
			stage('Publish') {
				withDockerRegistry(registry: [credentialsId: 'dockerhub']) {
					sh("docker tag ${service} ${tagToDeploy}")
					sh("docker push ${tagToDeploy}")
				}
			}
			stage('Deploy') {
				sh("sed -i.bak 's#BUILD_TAG#${tagToDeploy}#' ./deploy/staging/*.yml")
				container('kubectl') {
					sh("kubectl config set-context --current --namespace=jenkins")
					sh("kubectl get pod -A")
					sh("kubectl apply  --namespace=staging -f ./deploy/staging")
				}
			}
		}
	}
}
