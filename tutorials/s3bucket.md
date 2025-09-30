
oc new-project s3bucket

oc create secret generic aws-s3-credentials \
  --from-file=aws-s3-credentials.properties \
  --namespace s3bucket