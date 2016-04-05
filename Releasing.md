# Introduction #

This page is addressed at ACRA developers. It documents the steps that should be followed when creating a new ACRA release.

# Needed software and configuration #

  * Install [Maven](http://maven.apache.org/download.html)
  * Configure [Subversion](http://subversion.apache.org) to automatically add the file mime types as svn properties (used when uploading the Javadoc). To do so, edit ~/.subversion/config and add the following properties:
```
enable-auto-props = yes

### Section for configuring automatic properties.
[auto-props]
*.png = svn:mime-type=image/png
*.jpg = svn:mime-type=image/jpeg
*.gif = svn:mime-type=application/octet-stream
*.xml = svn:eol-style=native;svn:mime-type=text/xml
*.css = svn:eol-style=native;svn:mime-type=text/css
*.js = svn:eol-style=native;svn:mime-type=text/javascript
*.sql = svn:eol-style=native;svn:mime-type=text/plain
*.txt = svn:eol-style=native;svn:mime-type=text/plain
*.html = svn:eol-style=native;svn:mime-type=text/html
*.properties = svn:eol-style=native;svn:mime-type=text/plain
*.php = svn:eol-style=native;svn:mime-type=text/plain
*.tpl = svn:eol-style=native;svn:mime-type=text/html
*.ptpl = svn:eol-style=native;svn:mime-type=text/plain
```

If you are using Eclipse/Subversive, go to menu Window/Preferences/Team/SVN/Properties Configuration/Automatic Properties, remove all existing items and import [this file](http://acra.googlecode.com/svn/wiki/files/svn-mimetypes.conf).

_This procedure is merely a [copy & paste](http://code.google.com/p/maven-googlecode-plugin/wiki/MavenSiteDeployOnSVN)._

# Releasing #

To create a new release, simply enter the following commands, after replacing `<release-ver>` (e.g. **3.1.1**) and `<next-dev-ver>` (e.g **3.2**) with the good values.
```
mvn release:prepare -DdevelopmentVersion=<next-dev-ver>-SNAPSHOT -DreleaseVersion=<release-ver> -Dtag=acra-<release-ver>
# To cancel the release after prepare, you may use "mvn release:rollback".
mvn release:perform
svn checkout https://acra.googlecode.com/svn/javadoc
cp -r target/apidocs javadoc/<release-ver>
cd javadoc
svn add <release-ver>
svn commit -m "Uploading Javadocs for version <release-ver>"
```

And that's all!

# After releasing #

  * Go to https://oss.sonatype.org/index.html, login, close the Staging repository containing the version to be released, then select it and Release it.
  * The Javadoc should be available on [the svn](http://acra.googlecode.com/svn/javadoc).
  * Don't forget to **update** the various wiki pages.

# Deploying a snapshot #

You might want to deploy a snapshot, for instance to let users test a bugfix without having to create a stable release.

To do so, enter the following command :
```
mvn clean deploy
```
