const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

exports.sendNotification = functions.database.ref("/messages/{chatId}/{msgId}")
    .onCreate((snapshot, context) => {
      const chatIdd = context.params.chatId;
      const messageFrom = snapshot.val().senderId;
      const userIdd = chatIdd.replace(messageFrom, "");
      const status = admin.firestore().collection("users").doc(userIdd)
          .get().then((doc) => {
            const token = doc.data().deviceToken;
            console.log(token);
            return true;
          });
      return status;
    });
