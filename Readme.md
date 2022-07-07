# digiassistmailer
 Mailer for Digi Assist

gcloud functions deploy FUNCTION_NAME \
  --entry-point ENTRY_POINT \
  --runtime RUNTIME \
  --trigger-event "providers/cloud.firestore/eventTypes/document.write" \
  --trigger-resource "projects/YOUR_PROJECT_ID/databases/(default)/documents/messages/{pushId}"
  
  
gcloud functions deploy digiassist-mailer \
  --entry-point com.digiassist.functions.FirestoreMailer \
  --runtime java11 \
  --trigger-event "providers/cloud.firestore/eventTypes/document.write" \
  --trigger-resource "projects/digiassist-354909/databases/(default)/documents/ticket/{pushId}"
  
  gcloud functions deploy digiassist-mailer \
  --entry-point com.digiassist.functions.FirestoreMailer \
  --runtime java11 \
  --trigger-event "providers/cloud.firestore/eventTypes/document.write" \
  --trigger-resource "projects/gcms-oht29077u9-2022/databases/(default)/documents/ticket/{pushId}"
  
  
  https://github.com/GoogleCloudPlatform/java-docs-samples/blob/13f73339196fae1f9ca62c4f341937bbe22d90f3/functions/firebase/firestore/src/main/java/functions/FirebaseFirestore.java
  
  https://github.com/mailjet/mailjet-apiv3-java#authentication
  
  https://cloud.google.com/functions/docs/deploying/filesystem
  
  https://cloud.google.com/functions/docs/testing/test-cicd#functions-prepare-environment-java
  
  
  gcloud iam service-accounts add-iam-policy-binding gcms-oht29077u9-2022@appspot.gserviceaccount.com \
 --member serviceAccount:361531915277@cloudbuild.gserviceaccount.com \
 --role roles/iam.serviceAccountUser
 
 gcloud projects add-iam-policy-binding gcms-oht29077u9-2022 \
 --member serviceAccount:361531915277@cloudbuild.gserviceaccount.com \
 --role roles/cloudfunctions.developer