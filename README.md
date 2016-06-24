# Bandwidth java-bxml-demo

This app shows how to use Java band BXML to implement call and text features, plus call and text forwarding.  The app uses Spark, a micro framework for creating web applications, along with the Velocity template engine.  Any calls made to the number will be forwarded to Raleigh, NC weather station by default.  Texting the number forwards to another Bandwidth number.

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

The app was created by modifying the basic app provided in Heroku app in their ([Getting Started on Heroku with Java](https://devcenter.heroku.com/articles/getting-started-with-java#introduction)) article.

## Install / Configuration
Before installation you must have the following:
* A Bandwidth Application Platform account ([sign up here](https://catapult.inetwork.com/pages/signup.jsf))
* Your App Platform userId, apiToken and apiSecret ([see here](http://ap.bandwidth.com/docs/security/))

## 1-Click Deploy to Heroku
You'll need a heroku account and to also set the environment variables.
* BANDWIDTH_USER_ID (found on the account tab of the developer console)
* BANDWIDTH_API_TOKEN (found on the account tab of the developer console)
* BANDWIDTH_API_SECRET (found on the account tab of the developer console)
* PHONE_NUMBER (Bandwidth number associated with your app on the my apps tab)

These environment variables can be set in the settings tab of the Heroku dashboard, under Config Variables.

The app now includes the ability to leave a voicemail when the app's associated Bandwidth number is called.  The transcription part does not work currently due to a know bug with api.  The app keeps track of the 5 most recent voicemails.