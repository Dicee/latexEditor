# LaTexEditor

LaTeXEditor is a graphic text editor (JavaFX UI), which enables to compose LateX reports.

## Features

LaTeXEditor provides the following features :

- generate LaTeX code corresponding to basic elements (chapter, section, paragraphe, includegraphics...) from their raw content
- add/remove packages via a UI frame. Those elements are created and displayed in a tree view.
- include LaTeX custom defined commands
- provide shortcuts to most frequent mathematic symbols
- use LaTeX templates depending on some parameters via simple forms and a template language that still has to be clearly defined (but is already functional)
- preview generated LaTeX code with syntax coloring
- preview the document via Yap
- generate an output PDF file
- set language of the UI (currently available : EN, FR)
- set appearance of the UI

## Pre-requisites

### Compilation

- Scala 2.1 or later
- JDK 8 or later

### Use

- A jre with a version greater than 1.8 should do.
- MikTeX installed on the machine and present in the environment variable PATH

## Known issues

- the split pane resizes incorrectly when passing from the template view and the text editor view (or vice-versa)
- DVI preview (using latex command) and pdf generation (using pdflatex command) are incompatible for the use of image resources for \includegraphics. One solution is to convert all the images to eps format so that it will work for the DVI preview and follow this example to make the pdf compilation work :

\documentclass{article}
\usepackage{graphicx}
\usepackage{epstopdf}
\epstopdfsetup{update} % only regenerate pdf files when eps file is newer
\begin{document}
\includegraphics[width=\textwidth]{jsf} % loads sine-eps-converted-to.pdf
\end{document}

As far as I'm concerned, I just don't use the DVI preview anymore and open the generated pdf under Google Chrome and refresh it each time needed. It could become a long term solution, requiring the user to define some alias to a pdf reader and call it through a ProcessBuilder.
