import java.io._
import scala.io._
import scala.collection.mutable._
import java.nio._
import java.lang.{ProcessBuilder => PB}
import scala.util.matching.Regex

import org.yaml.snakeyaml.Yaml

object blog_post {
  def slugify(fname: String): String = {
    val deslug = "([^-]*)-([^-]*)-([^-]*)-(.*)\\.([^.]*)".r
    fname match {
      case deslug(y, m, d, n, e) => y+"/"+m+"/"+d+"/"+n
      case _ => fname
    }
  }
  def deslugify(fname: String): String = {
    val deslug = "([^-]*)-([^-]*)-([^-]*)-(.*)\\.([^.]*)".r
    fname match {
      case deslug(y, m, d, n, e) => n
      case _ => fname
    }
  }
  def main(args: Array[String]) {
    if (args.length == 0)
    {
      usage
      return
    }
    val help = "(help|-h|--help|-?|/h)".r
    args(0) match {
      case help(opt) => {
        if (args.length == 1)
          usage
        else
          args(1) match {
            case "tags"    => tags_usage
            case "ls"      => ls_usage
            case "publish" => publish_usage
            case _         => usage
          }
      }
      case "ls"   => {
        var stack = args.slice(1, args.length)
        var verbose = true
        var show_published = true
        var show_unpublished = false
        var pattern = ".*".r
        stack.foreach(x => x match {
          case "-t"          => verbose = false
          case "-v"          => verbose = true
          case "-a"          => { show_published = true;  show_unpublished = true  }
          case "-u"          => { show_published = false; show_unpublished = true  }
          case "-p"          => { show_published = true;  show_unpublished = false }
          case (pat: String) => pattern = pat.r
        })
        ls(verbose, show_published, show_unpublished, pattern)
      }
      case "publish" => publish(args.slice(1, args.length): _*)
      case "tags"    => tags
      case _         => usage
    }
  }
  def usage = {
    println("""
Usage: blog.sh [--version] [--help] COMMAND [Arguments]
  The most commonly used commands are:
    help     - Get help for a particular command
    init     - Create a new empty blog
    ls       - Show list of blogs
    tag      - Add tags to a post
    tags     - Regenerates tags from yaml headers in published posts.
    mp       - Make a micro post from standard in
    post     - Make a new unpublished post
    publish  - Publish an unpublished post
    untag    - Remove tags from a post
    
""")
  }
  def publish_usage = {
    println("""
Usage: blog.sh publish [name*]
  Publish each named blog entry.
""")
  }
  def publish(names: String*) = {
    names.foreach(name => {
      val dp = new PB("date", "+%Y-%m-%d-").start
      val out = dp.getInputStream()
      dp.waitFor
      var date: Array[Byte] = new Array[Byte](out.available)
      out.read(date)
      var date_s = new String(date)
      new PB("git", "mv", "unpublished/"+name, "_posts/"+date_s+name).start.waitFor
      new PB("git", "commit", "unpublished/"+name, "_posts/"+date+name, "-m", "Post "+name).start.waitFor
    })
  }
  def ls_usage = {
    println("""
Usage: blog.sh ls [-t|-v] [-p|-u|-a] [pattern]
  List blog posts
    -t            Print title only
    -v (default)  Print full slug
    -p            Print only published blog posts
    -u            Print only unpublished blog posts
    -a            Print all blog posts
""")
  }
  def ls(verbose: Boolean = true, show_published: Boolean = true, show_unpublished: Boolean = false, pattern: Regex = ".*".r)  = {
    val posts_filter = "[a-zA-Z0-9].*\\.(md|html)".r
    val published = new File("_posts").listFiles.filter(f => f.getName match { case posts_filter(post_type) => true; case _ => false })
    val unpublished = new File("unpublished").listFiles.filter(f => f.getName match { case posts_filter(post_type) => true; case _ => false })
    if (show_published)
      published.foreach(x => x.getName match { case pattern() => println(if (verbose) x.getName else deslugify(x.getName)); case _ =>  })
    if (show_unpublished)
      unpublished.foreach(x => x.getName match { case pattern() => println(x.getName); case _ =>  })
  }
  def tags_usage = {
    println("""
Usage: blog.sh tags
    Regenerate the tags in the tags directory from the yaml headers of published posts.""")
  }

  def tags = {
    // clean tags directory
    val test = ".*\\.(xml|html)".r
    val to_clean = (new File("tags").listFiles.filter(x => x.getName match { case test(e) => true; case _ => false }))
    to_clean.foreach(f => f.delete)

    // create tag files
    var tags2posts: collection.mutable.Map[String, MutableList[(String, String)]] = new collection.mutable.HashMap[String, MutableList[(String, String)]]()
    var dir_posts = new File("_posts")
    val subfiles = dir_posts.listFiles
    type Yml = java.util.LinkedHashMap[String, Any] //Map[String, Either[String, Array[String]]]
    var data: MutableList[(String, Yml)] = new MutableList[(String, Yml)]()
    println("Creating tag files.")
    val posts_filter = "[a-zA-Z0-9].*\\.(md|html)".r
    subfiles.filter(f => f.getName match { case posts_filter(post_type) => true; case _ => false }).foreach( f => {
      var src = Source.fromFile(f)
      var lines = src.getLines()
      var line = lines.next
      if (line != "---")
        System.exit(1);
      var buf: StringBuilder = new StringBuilder()
      line = lines.next
      while(line != "---") {
        buf = buf.append(line + "\n")
        line = lines.next
      }
      var yaml_string = buf.toString()
      var yaml = new Yaml()
      var ret: Yml = yaml.load(yaml_string).asInstanceOf[Yml]
      data.+=((f.getName, ret))
      src.close;
    }) 
    println(data)
    for((k, v) <- data) {
      var tags: Array[String] = new Array[String](1);
      tags = v.get("tags").asInstanceOf[java.util.ArrayList[String]].toArray(tags)
      val title = v.get("title").asInstanceOf[String]
      for(tag <- tags) {
        var pre: MutableList[(String, String)] = new MutableList[(String, String)]()
        if(tags2posts.contains(tag))
          pre = tags2posts(tag)
        pre += ((k, title))
        tags2posts(tag) = pre;
      }
    }
    println(tags2posts)
    /*for((tag, posts) <- tags2posts) {
      var tagfile = new File("tags/" + tag + ".html")
      var fout = new FileWriter(tagfile);
      val page = """
<?xml version="1.0"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>Pages tagged '"""+ tag +"""'</title>
    <link rel="stylesheet" type="text/css" href="http://flaviusb.net/style.css"/>
    <link rel="stylesheet" type="text/css" href="http://flaviusb.net/container.css"/>
    <link rel="stylesheet" type="text/css" href="http://flaviusb.net/mono.css"/>
    <link href='http://fonts.googleapis.com/css?family=Inconsolata' rel='stylesheet' type='text/css'>
    <link rel="shortcut icon" href="/favicon.ico" type="image/x-icon">
    <link rel="icon" href="http://flaviusb.net/favicon.ico" type="image/x-icon">
    <link href='http://flaviusb.net/tags/"""+tag+""".xml'  type="application/atom+xml" rel="alternate" title='Blog Atom Feed for tag: """+tag+"""' />
    <meta http-equiv="content-type" content="application/xhtml+xml; charset=utf-8" />
  </head>
  <body>
    """ + posts.foldLeft("<ol>\n")((head, post) => head + "      <li><a href='http://flaviusb.net/"+slugify(post._1)+"'>"+post._2+"</a></li>\n") + """
    </ol>
  </body>
</html>
"""
//"""
      fout.write(page)
      fout.flush
      fout.close
    }*/
    for((tag, posts) <- tags2posts) {
      var tagfile = new File("tags/" + tag + ".xml")
      var fout = new FileWriter(tagfile);
      var page = """---
layout: nil
tag: """+tag+"""
---
<?xml version="1.0" encoding="utf-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
 
 <title>Justin (:flaviusb) Marsh</title>
 <link href="http://flaviusb.net/tags/{{ page.tag }}.xml" rel="self"/>
 <link href="http://flaviusb.net"/>
 <updated>{{ site.time | date_to_xmlschema }}</updated>
 <id>http://flaviusb.net/tags/{{ page.tag }}</id>
 <author>
   <name>Justin (:flaviusb) Marsh</name>
   <email>justinius.marsh@gmail.com</email>
 </author>

 {% for post in site.posts %}
 {% if post.tags contains page.tag %}
 <entry>
   <title>{{ post.title }}</title>
   <link href="http://flaviusb.net{{ post.url }}"/>
   <updated>{{ post.date | date_to_xmlschema }}</updated>
   <id>http://flaviusb.net{{ post.id }}</id>
   <content type="html">{{ post.content | xml_escape }}</content>
 </entry>
 {% endif %}
 {% endfor %}
 
</feed>"""
      fout.write(page)
      fout.flush
      fout.close
      tagfile = new File("tags/" + tag + ".html")
      fout = new FileWriter(tagfile);
      page = """---
layout: nil
tag: """+tag+"""
---
<?xml version="1.0"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>Pages tagged {{ page.tag }}</title>
    <link rel="stylesheet" type="text/css" href="http://flaviusb.net/style.css"/>
    <link rel="stylesheet" type="text/css" href="http://flaviusb.net/container.css"/>
    <link rel="stylesheet" type="text/css" href="http://flaviusb.net/mono.css"/>
    <link href='http://fonts.googleapis.com/css?family=Inconsolata' rel='stylesheet' type='text/css'>
    <link rel="shortcut icon" href="/favicon.ico" type="image/x-icon">
    <link rel="icon" href="http://flaviusb.net/favicon.ico" type="image/x-icon">
    <link href='http://flaviusb.net/tags/{{ page.tag }}.xml'  type="application/atom+xml" rel="alternate" title='Blog Atom Feed for tag: {{ page.tag }}' />
    <meta http-equiv="content-type" content="application/xhtml+xml; charset=utf-8" />
  </head>
  <body>
    <h2>Posts tagged: {{ page.tag }}</h2> 
    <ul class="posts">
    {% for post in site.posts %}
    {% if post.tags contains page.tag %}
      <li><span>{{ post.date | date_to_string }}</span> &raquo; <a href="{{ post.url }}">{{ post.title }}</a> &nbsp; &nbsp; <span>{% for tag in post.tags %} <a href="http://flaviusb.net/tags/{{ tag }}">{{ tag }}</a> &nbsp; {% endfor %}</span></li>
    {% endif %}
    {% endfor %}
    </ul><br>

    <a href="http://flaviusb.net">Home</a>   |   <a href="http://github.com/flaviusb">Code</a>
  </body>
</html>
"""
      fout.write(page)
      fout.flush
      fout.close
    }
    new PB("git", "add", "--all", "tags/*.html", "tags/*.xml").start.waitFor
    new PB("git", "commit", "tags/*.html", "tags/*.xml", "-m", "Automatic commit of autogenerated tag pages.").start.waitFor
  }
}

