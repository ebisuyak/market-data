def toKubernetes(tagToDeploy, namespace, deploymentName) {
	sh("sed -i.bak 's#BUILD_TAG#${tagToDeploy}#' ./deploy/${namespace}/*.yml")
	container('kubectl') {
		kubectl("apply -f ./deploy/${namespace}")
	}
}

def kubectl(namespace, command) {
	sh("kubectl --namespace=${namespace} ${command}")
}

def rollback(deploymentName) {
	kubectl("rollout undo deployment/${deploymentName}")
}

return this;
