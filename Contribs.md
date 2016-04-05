# ACRA has moved to GitHub #
**The project home page is now http://acra.ch
Wiki, source code, issue tracker and everything else is now based at http://github.com/ACRA/acra.**

**_The following documentation is deprecated and kept here to prevent the internets from melting._**


---



# Contributions #

Here are contributions developed by ACRA users. If you want to submit your own work, you can introduce it to the [acra-discuss group](http://groups.google.com/group/acra-discuss).

## ReportSender implementations ##

With ACRA v4 you can create [your own ReportSender](http://code.google.com/p/acra/wiki/AdvancedUsage#Implementing_your_own_sender) implementations.

Here is a list of contributed ReportSenders:

  * JSON Sender : [sender](https://github.com/x2on/android-acra-json-sender) + [PHP server app](https://github.com/x2on/android-acra-server) by [Felix Schulze](https://www.felixschulze.de/) / [X2ON](http://www.x2on.de/)

## Reports analysis tools ##

Some people are working on tools to help you sort, filter, clean, analyze your reports:

  * [Analysing Android error reports using the Google visualisation API](http://zegoggl.es/2011/01/analysing-android-error-reports-using-the-google-visualisation-api.html) by [Jan Berkel](http://zegoggl.es/about.html)
  * [LogTracker](https://github.com/MegaDevs/ACRA_LogTracker) : a Java desktop app for reports filtering by [MegaDevs](http://www.megadevs.com/megadevs/)
  * [Code sample to filter/delete/colorize spreadsheet's entries](https://groups.google.com/d/msg/acra-discuss/6FxV1TuC0F4/Nb_lc9dU__cJ) by mot12 on [acra-discuss](http://groups.google.com/group/acra-discuss)

## Alternative backends ##

  * [Bugsense](http://www.bugsense.com) Android, iOS, AppEngine and Windows Phone bug analysis platform
  * https://github.com/sghael/ACRA-Catcher a Rails report catcher, developed by [the Ravid team](http://www.getravid.com/)
  * [LogEntries](http://www.vaudaux-ruth.com/android-log-collecting-with-acra-and-logentries), log management service
  * [Hockeyapp](http://support.hockeyapp.net/kb/client-integration-android/how-to-use-acra-with-hockeyapp) OSX, iOS and Android beta distribution platform and crash reports collection.
  * [Zubhium](http://www.zubhium.com) , platform dedicated to Android applications beta distribution, crash analytics and users support.
  * https://github.com/BicouQ/crashreportsviewer/blob/master/README.md basic opensource PHP backend by Benoit Duffez

## Scripting GDocs spreadsheet ##

This is an area where lots of contribution could be made but unfortunately there are few people writing google apps scripts.

### Delete oldest rows automatically ###
by Ronen

As google spreadsheets are limited to 400K cells, it's a good idea to
have a script that cleans it automatically. If you reach the limits
your sheet may not open anymore!
This script can be triggered periodically automatically.
Simply go to Tools->Script Editor and paste the following script. Then
go to Resources->Current script's triggers and set your time-driven
trigger.
From what I've read, you won't be able to keep more than ~11K rows
without reaching the limits.
BE CAREFUL AND CHANGE THE SCRIPT FOR YOUR NEEDS AS IT WILL DELETE ROWS
FROM YOUR SPREADSHEET!

```
function acraCleanup() {
  var rowsToKeep = 5000; //CHANGE TO YOUR DESIRED NUMBER OF ROWS TO
KEEP.
  var rows = SpreadsheetApp.getActiveSheet().getLastRow();
  var numToDelete = rows - rowsToKeep  -1;
  if (numToDelete > 0) SpreadsheetApp.getActiveSheet().deleteRows(2, numToDelete);
}
```