import java.io._
import scala.io._
import scala.collection.mutable._
import java.nio._

import org.yaml.snakeyaml.Yaml

object gentags {
  def slugify(fname: String): String = {
    val deslug = "([^-]*)-([^-]*)-([^-]*)-(.*)\\.([^.]*)".r
    fname match {
      case deslug(y, m, d, n, e) => y+"/"+m+"/"+d+"/"+n
      case _ => fname
    }
  }
  def main(args: Array[String]) {
    var tags2posts: collection.mutable.Map[String, MutableList[(String, String)]] = new collection.mutable.HashMap[String, MutableList[(String, String)]]()
    var dir_posts = new File("_posts")
    val subfiles = dir_posts.listFiles
    type Yml = java.util.LinkedHashMap[String, Any] //Map[String, Either[String, Array[String]]]
    var data: MutableList[(String, Yml)] = new MutableList[(String, Yml)]()
    println("foo")
    subfiles.foreach( f => {
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
    for((tag, posts) <- tags2posts) {
      var tagfile = new File("tags/" + tag + ".md")
      var fout = new FileWriter(tagfile);
      val page = """---
title: "Pages tagged '"""+ tag +"""'"
layout: tag
---
""" + posts.foldLeft("")((head, post) => head + "- ["+post._2+"](http://flaviusb.net/"+slugify(post._1)+")\n")
//"""
//"""
      fout.write(page)
      fout.flush
      fout.close
    }
  }
}


