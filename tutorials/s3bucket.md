
oc new-project s3bucket

oc create secret generic aws-s3-credentials \
  --from-file=aws-s3-credentials.properties \
  --namespace s3bucket

  what a wild review but

  Kamelet properties map to kameletbinding.

  knative 'traits' auto sync / discover in the cluster... not sure if this isn't working
  using url (local)

  the integration is a builder of a kamelete based upon binding and sink/ source components connected.  the resulting image needs a managed image registry to connect to.

  

  trigger filter:
  org.apache.camel.event.aws.s3.getObject



  export FUNC_REGISTRY=docker.io/axodevelopment

  I am on mac so i'll use buildx

  docker buildx build --platform linux/amd64 -t index.docker.io/axodevelopment/s3-json-processor:latest --push .

  kn service update s3-json-processor --image index.docker.io/axodevelopment/s3-json-processor:latest --annotation "rollout-ts=$(date +%s)"