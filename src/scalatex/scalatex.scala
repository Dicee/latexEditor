package scalatex

import java.io.File
import java.io.FileReader
import latex.LateXMaker
import latex.elements.LateXElement
import utils.TokenReader
import scala.collection.mutable.ArrayBuffer
import scala.sys.process._
import scala.collection.JavaConversions._
import latex.elements.Templates

object scalatex extends App {
    val lm            = new LateXMaker
    val latexElements = ArrayBuffer[LateXElement]()
    
    try {
    	Templates.init();
    	args.toList match {
    		case "-tex" :: x :: y :: Nil => toTex(x,y)
    		case "-pdf" :: x :: y :: Nil => toPdf(x,y)
    		case x :: _ if x(0) == '-'   => printUsage("Uknown output format " + x)
    		case _                       => printUsage()
    	}
    } catch {
    	case t: Throwable => println(t.getMessage)
	}
	
	def toTex(input: String, output: String) = {
		val (in,out) = (getPath(input,output,".tex"))
	    fromJavatex(in,out)
	    out
	}
	
	def toPdf(input: String, output: String) = {
		val (in,out) = (getPath(input,output,".pdf"))
		val tex     = toTex(input,output.replace(".pdf",".tex"))
		Seq("pdflatex","-halt-on-error",tex.getCanonicalPath).!
	}
	
	def getPath(input: String, output: String, ext: String) = {
	    if (!input.endsWith(".javatex")) 
	        throw new IllegalArgumentException("Unrecognized input file extension (.javatex expected)")
	    else if (output != ".") (new File(input),new File(if (output.endsWith(ext)) output else output + ext))
	    else                    (new File(input),new File(input.substring(0,input.lastIndexOf('.')) + ext))
	}
	
	def fromJavatex(in: File, out: File) = {
        lm.getParameters.clear
        latexElements   .clear
        
        val tr      = new TokenReader(new FileReader(in),"##")
        val buffer  = for (x <- tr) yield x.trim
        val decl    = buffer.zipWithIndex.filter(_._2 % 2 == 0).map(_._1)
        val content = buffer.zipWithIndex.filter(_._2 % 2 == 1).map(_._1)
        decl.zip(content).foreach { case (a,b) => newLateXElement(a,b) }
        lm.makeDocument(out,latexElements)
    }
	
	def newLateXElement(decl: String, content: String) = decl match {
	    case "packages"         => lm.getParameters.addPackages (content.split("[;\\s+]|;\\s+"): _*)
	    case "commands"         => lm.getParameters.include     (content.split("[;\\s+]|;\\s+"): _*)
	    case "documentSettings" => lm.getParameters.loadSettings(content)
	    case _                  => 
	        val regex = "\\s*(>*)\\s*(\\w+)\\s*(\\[(.*)\\])?\\s*".r
	        val nullOrEmpty = (s: String) => s == null || s.isEmpty
	        regex.findFirstMatchIn(decl) match {
	        	case None    =>
	            case Some(m) => latexElements += 
	                LateXElement.newLateXElement(m.group(2) + (if (nullOrEmpty(m.group(4))) "" else m.group(3)),content)
	        }
	}
	
	def printUsage(msg: String="") = println("%s\nUsage : scalatex [-tex|-pdf] input_path output_path".format(msg))
	
	implicit class ScalaTokenReader(tr: TokenReader) extends Iterable[String] {
	    override def iterator = new Iterator[String]() {
	        var _next = tr.readToNextToken
	    	override def next = {
	            val res = _next
	            _next = tr.readToNextToken
	            res
	        }
	        override def hasNext = _next == null
	    }
	}
}