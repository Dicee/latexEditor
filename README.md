# LaTexEditor

LaTeXEditor is a graphic text editor (JavaFX UI), which enables to compose LateX reports.

## Features

LaTeXEditor provides the following features :

- simple representation of the LaTeX code, called *javatex*. Not all features of LaTeX are supported (far from...) but it is possible to directly include LaTeX code when necessary
- generate LaTeX code corresponding to basic elements (chapter, section, paragraphe, includegraphics...) from their javatex representation
- add/remove packages via a UI frame. Those elements are created and displayed in a tree view.
- include LaTeX custom defined commands
- provide shortcuts to most frequent mathematic symbols
- use LaTeX templates depending on some parameters via simple forms and a template language that still has to be clearly defined (but is already functional)
- preview generated LaTeX code with syntax coloring
- preview the document via Yap
- generate an output PDF file
- set language of the UI (currently available : EN, FR)
- set appearance of the UI
- command-line mode (Scala application) enabling to output a tex and/or a pdf file from a javatex document

## Pre-requisites

### Compilation

- Scala 2.11 or later
- JDK 8 or later

### Use

- A jre with a version greater than 1.8 should do.
- MikTeX installed on the machine and present in the environment variable PATH

## javatex

### Syntax

**javatex** is a simple format used to save and represent our documents. Here is an example of its very basic syntax :

```javatex
packages ##
one
package
per[line]
##
commands ##
pathFromIncludeDirectoryToAFileContainingLaTeXCommandDefinitions
anotherOne
etc
##
documentSettings ##
documentClass=report
alinea=4mm
chapterName=Partie
##
title[titlePage.bicolorHeaderTemplate] ##
first_author_name=Albert \textsc{Einstein}
title_size=0.8
sup_strip_color=0.80,0.80,0.80
description=Blablabla
inf_strip_color=0.00,0.00,0.00
title=Blablabla
##
> chapter ##
This is a chapter
##
>> section ## 
This is a section in the chapter
##
>>> list ##
This is a;
list with several;
items separated by semi-colons
##
>>> image ##
path.png;
This is the caption of an included image. Below is the scale of the image in the document;
0.8
##
>>> latex ##
\begin{center}
Here you can insert raw LaTeX code
\end{center}
##
> section ##
This section is still related to the same chapter, but will be seen at the same level as the chapter in the UI
##
>> code ##
javascript
function f() {
  console.log("Insert any code here, set the language on the first line");
}
##
>> template[templateFamily.templateName] ##
actuallyTheTitleAlso=IsATemplate
soItsJust=TheSame
##
```
The `>` symbols are only meant to allow the UI to identify the structure of the document, but they are unnecessary when working with command-line only. Yet, it is recommended to use it as it also improves the readability. All the keywords are presented in the sample of code provided. The `commands` have to be stored in the `LATEX_HOME/includes` directory (environment variable to define) and the templates in the `LATEX_HOME/includes/templates` directory. For a file with the following path : `LATEX_HOME/includes/templates/titlePage/header/header.template`, the code will look like :

```javatex
template[titlePage.headerTemplate] ##
##
```

Some templates and other includes are provide in the `includes` folder of this repository.

## Templating language

The templating language is a very basic language that enables to specify where, when and how some elements of a LaTeX template should be modified. Here are some examples :

```latex
\DeclareFixedFont{\bigsf}{T1}{phv}{b}{n}{${title_size}cm}
 ${first_author_name} {?first_author_info!=null? \\
	${first_author_info}?}
```

Basically, `${name}` indicates that the value of the parameter `name` of the template should be printed here. `{?name!=null? anything ?}` means that `anything` should be printed if and only if `name != null`. For now, no other types of condition are supported, nor are inner `{?? ?}`. To use templates with the interface, a `lang` directory should be added to the directory of the template and provide a property file for each supported languages. The properties will have the same name as the name of the parameters of the template.

## Known issues

- the split pane resizes incorrectly when passing from the template view to the text editor view (or vice-versa)
- DVI preview (using latex command) and pdf generation (using pdflatex command) are incompatible for the use of image resources with `\includegraphics`. One solution is to convert all the images to eps format so that it will work for the DVI preview and follow this example to make the pdf compilation work :

```latex
\documentclass{article}
\usepackage{graphicx}
\usepackage{epstopdf}
\epstopdfsetup{update} % only regenerate pdf files when eps file is newer
\begin{document}
\includegraphics[width=\textwidth]{sine} % loads sine-eps-converted-to.pdf
\end{document}
```

As far as I'm concerned, I just don't use the DVI preview anymore and open the generated pdf under Google Chrome and refresh it each time needed. It could become a long term solution, requiring the user to define some alias to a pdf reader and call it through a ProcessBuilder.
