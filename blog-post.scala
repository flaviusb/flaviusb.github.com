import java.io._
import scala.io._
import scala.collection.mutable._
import java.nio._
import java.lang.{ProcessBuilder => PB}
import scala.util.matching.Regex

import org.yaml.snakeyaml.{Yaml, DumperOptions}

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
            case "tag"     => tag_usage
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
      case "tag"     => {
        if(args.length < 3)
          tag_usage
        else
        {
          var published = false
          var unpublished = true
          var stack = args.slice(1, args.length)
          stack(0) match {
            case "-a"          => { stack = stack.slice(1, stack.length); published = true;  unpublished = true  }
            case "-u"          => { stack = stack.slice(1, stack.length); published = false; unpublished = true  }
            case "-p"          => { stack = stack.slice(1, stack.length); published = true;  unpublished = false }
            case _ => 
          }
          if (stack.length < 2)
            tag_usage
          else
            tag(published, unpublished, stack(0).r, stack.slice(1, stack.length): _*)
        }
      }
      case "untag"     => {
        if(args.length < 3)
          untag_usage
        else
        {
          var published = false
          var unpublished = true
          var stack = args.slice(1, args.length)
          stack(0) match {
            case "-a"          => { stack = stack.slice(1, stack.length); published = true;  unpublished = true  }
            case "-u"          => { stack = stack.slice(1, stack.length); published = false; unpublished = true  }
            case "-p"          => { stack = stack.slice(1, stack.length); published = true;  unpublished = false }
            case _ => 
          }
          if (stack.length < 2)
            untag_usage
          else
            untag(published, unpublished, stack(0).r, stack.slice(1, stack.length): _*)
        }
      }
      case "mp"     => {
        var defer = false
        var title = ""
        var tags = Array("")
        var body = ""
        var stack = args.slice(1, args.length)
        var skip = false;
        var die = false
        args.slice(1, args.length).foreach(s => if (! skip) s match {
          case "-d"          => { stack = stack.slice(1, stack.length); defer = true  }
          case "-t"          => { if (stack.length<1) { die = true } else { title = stack(1); stack = stack.slice(2, stack.length); skip = true; }  }
          case "-@"          => { if (stack.length<1) { die = true } else { tags = stack(1).split(","); stack = stack.slice(2, stack.length); skip = true; } }
          case _ => 
        } else skip = false)
        if (! die)
          mp(defer, title, tags, if (stack.length > 0) stack(0) else "")
        else
          mp_usage
      }
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
    mp       - Make a micro post from the command line
    post     - Make a new unpublished post
    publish  - Publish an unpublished post
    untag    - Remove tags from a post
    
""")
  }
  def mp_usage = {
    println("""
Usage: blog.sh mp [-d] [-t title] [-@ tags] [BODY]
  Publish a snippet from the command line
    -d        Defer publication (ie place this in unpublished).
    -t title  Title the post title. Default is Untitled.
    -@ tags   Comma separated list of tags.
    [BODY]    A quoted body of the post. If absent, read from standard in.
""")
  }
  def mp(defer: Boolean = false, title: String = "Untitled", tags: Array[String] = Array(""), body: String = "") = {
   val header = """---
layout: post
title: """ + title +"""
tags:""" + tags.foldLeft("")((x:String, y: String) => x + "\n- " + y) + """
---
"""
    println(header)
    var body2 = body
    if (body == "") {
      println("Enter blog post. Finish with a newline.")
      body2 = readLine
    }
    val name = title.replaceAll(" ", "-") + ".md"
    val date_s = s_date
    var path = (if (defer) { "unpublished/" } else { "_posts/" + date_s })
    val file = new File(path + name)
    var fout = new FileWriter(file)
    fout.write(header)
    fout.write(body2)
    fout.flush
    fout.close
  }
  def publish_usage = {
    println("""
Usage: blog.sh publish [name*]
  Publish each named blog entry.
""")
  }
  def s_date = {
    val dp = new PB("date", "+%Y-%m-%d-").start
    val out = dp.getInputStream()
    dp.waitFor
    var date: Array[Byte] = new Array[Byte](out.available)
    out.read(date)
    var date_s = new String(date.slice(0, date.length - 1))
    date_s
  }
  def publish(names: String*) = {
    names.foreach(name => { 
      var date_s = s_date
      new PB("git", "mv", "unpublished/"+name, "_posts/"+date_s+name).start.waitFor
      new PB("git", "commit", "unpublished/"+name, "_posts/"+date_s+name, "-m", "Post "+name).start.waitFor
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
  def tag_usage = {
    println("""
Usage: blog.sh tag [-p|-u|-a] [pattern] [tag*]
    Add tags to the yaml headers of posts which meet pattern.
      -p             Only add to published posts
      -u  (default)  Only add to unpublished posts
      -a             Add to any posts
""")
  }
  def add_tags(f: File, tags: String*) = {
    println("Tagging "+f.getName)
    type Yml = java.util.LinkedHashMap[String, Any]
    //var data: MutableList[(String, Yml)] = new MutableList[(String, Yml)]()
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
    var rest: scala.collection.mutable.Buffer[String] = new scala.collection.mutable.ListBuffer[String]()
    rest.++=(lines)
    var yaml_string = buf.toString()
    var options: DumperOptions = new DumperOptions()
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
    var yaml = new Yaml(options)
    var ret: Yml = yaml.load(yaml_string).asInstanceOf[Yml]
    src.close;
    // We have to work around the Java collection idioms here
    var intarr = ret.get("tags").asInstanceOf[java.util.ArrayList[String]]
    tags.foreach(tag => if (intarr.indexOf(tag) == -1) intarr.add(tag))
    ret.put("tags", intarr)
    println(ret)
    var fout = new FileWriter(f)
    fout.write("---\n")
    fout.write(yaml.dump(ret))
    fout.write("---\n")
    rest.foreach(chunk => fout.write(chunk+"\n"))
    fout.flush
    fout.close
  }
  def tag(p: Boolean, u: Boolean, pattern: Regex, tag: String*) = {
    println("tagging "+pattern)
    val posts_filter = "[a-zA-Z0-9].*\\.(md|html)".r
    val published = new File("_posts").listFiles.filter(f => f.getName match { case posts_filter(post_type) => true; case _ => false }).filter(f =>
      f.getName match { case pattern() => true; case _ => false }
    )
    val unpublished = new File("unpublished").listFiles.filter(f => f.getName match { case posts_filter(post_type) => true; case _ => false }).filter(f =>
      f.getName match { case pattern() => true; case _ => false }
    )
    println("Applying tags: "+tag.toString)
    if (p)
      published.foreach(x => add_tags(x, tag: _*))
    if (u)
      unpublished.foreach(x => add_tags(x, tag: _*)) 
  }
  def untag_usage = {
    println("""
Usage: blog.sh untag [-p|-u|-a] [pattern] [tag*]
    Remove tags from the yaml headers of posts which meet pattern.
      -p             Only remove from published posts
      -u  (default)  Only remove from unpublished posts
      -a             Remove from any posts
""")
  }
  def remove_tags(f: File, tags: String*) = {
    println("Untagging "+f.getName)
    type Yml = java.util.LinkedHashMap[String, Any]
    //var data: MutableList[(String, Yml)] = new MutableList[(String, Yml)]()
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
    var rest: scala.collection.mutable.Buffer[String] = new scala.collection.mutable.ListBuffer[String]()
    rest.++=(lines)
    var yaml_string = buf.toString()
    var options: DumperOptions = new DumperOptions()
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
    var yaml = new Yaml(options)
    var ret: Yml = yaml.load(yaml_string).asInstanceOf[Yml]
    src.close;
    // We have to work around the Java collection idioms here
    var intarr = ret.get("tags").asInstanceOf[java.util.ArrayList[String]]
    tags.foreach(tag => while (intarr.indexOf(tag) != -1) intarr.remove(tag))
    ret.put("tags", intarr)
    println(ret)
    var fout = new FileWriter(f)
    fout.write("---\n")
    fout.write(yaml.dump(ret))
    fout.write("---\n")
    rest.foreach(chunk => fout.write(chunk+"\n"))
    fout.flush
    fout.close
  }
  def untag(p: Boolean, u: Boolean, pattern: Regex, tag: String*) = {
    println("untagging "+pattern)
    val posts_filter = "[a-zA-Z0-9].*\\.(md|html)".r
    val published = new File("_posts").listFiles.filter(f => f.getName match { case posts_filter(post_type) => true; case _ => false }).filter(f =>
      f.getName match { case pattern() => true; case _ => false }
    )
    val unpublished = new File("unpublished").listFiles.filter(f => f.getName match { case posts_filter(post_type) => true; case _ => false }).filter(f =>
      f.getName match { case pattern() => true; case _ => false }
    )
    println("Unapplying tags: "+tag.toString)
    if (p)
      published.foreach(x => remove_tags(x, tag: _*))
    if (u)
      unpublished.foreach(x => remove_tags(x, tag: _*)) 
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

