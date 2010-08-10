---
title: "Whitespace and XML file manipulation in Scala"
layout: post
---

Sometimes, when one wants to write a script to manipulate xml files, one does not only care about the abstract xml infoset. The existing layout can be important if
people edit the xml by hand, or the size of the diff is important. The stock answer here is often 'use a regex'. This really didn't appeal to me; I'd much rather use a programmatic transform, and avoid the problematic edge cases that regexen cannot catch, even if I had to do stuff in a pretty limited way in order to get the small diffs. So, I wrote a wrapper for producing augmented xml objects, and a wrapper around scala.xml.transform.RewriteRule to allow me to manipulate them. The result is ugly and brittle in a library sense but, importantly, not in a permutations of structure way.
