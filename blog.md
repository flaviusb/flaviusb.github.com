---
layout: container
title: (flaviusb get blog) each [entry | Transcript print entry]
---
##Entries.##


<ul class="posts">
    {% for post in site.posts %}
      <li><span>{{ post.date | date_to_string }}</span> &raquo; <a href="{{ post.url }}">{{ post.title }}</a><span>{% for tag in post.tags %} <a href="http://flaviusb.net/tags/{{ tag }}">{{ tag }}</a> &nbsp {% endfor %}</span></li>
    {% endfor %}
</ul>



[Home](http://flaviusb.net)   |   [Code](http://github.com/flaviusb)
