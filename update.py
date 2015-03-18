import sys
import shutil
import re
import os
from subprocess import call

def load_properties(path):
	res = {}
	with open(path,"r") as lines:
		for line in lines:
			split                 = line.split("=")
			res[split[0].strip()] = split[1].strip()
	return res
			
def read_regexps(args,root):
	def get_pattern(file):
		if os.path.isdir(file):
			start = file.count(os.sep)
			# determine the depth of the directory
			depth = start + 1
			for top, dirs, files in os.walk(file):
				depth = max(top.count(os.sep) - start,depth)
			
			patterns = ["/*"]
			for i in range(1,depth):
				patterns.append(patterns[i-1] + "/*")
			return [ absolute_path(root,file) + x for x in patterns ]
		else:
			return [ file ]
	
	def get_regex(s):
		return "^" + root + "/" + s.replace(".","\.").replace("*","[^/]+") + "$"
			
	return [ re.compile(item) for arg in args for item in get_pattern(arg) ]

def accept(name,ignore):
	return not(name.startswith(".")) and name not in ignore and not(name.endswith("jar"))
	
def absolute_path(root,subfile):
	return subfile if root == '.' else root + "/" + subfile
	
def fetch_files(ignore,root):		
	if not os.path.isdir(root):
		return frozenset({ root }) if accept(root,ignore) else frozenset()
		
	files = { s for s in os.listdir(root) if accept(s,ignore) }
	return { x for y in files for x in fetch_files(ignore,absolute_path(root,y)) }
	
def filter_files(regs,files):
	matches = []
	for reg in regs:
		matches += [ file for file in files if reg.match(file) ]
		
	if matches:
		print(len(matches),"matches")
	else:
		print("Found no file matching any of these regular expressions")
	return matches
	
def copy_files(files,from_dir,to_dir):
	# make a backup copy, because I'm cautious : even MY code may contain bugs
	if not(os.path.exists(to_dir + "/bak")):
		os.mkdir(to_dir + "/bak")
		
	current = os.getcwd()
	os.chdir(to_dir)
	call([ "jar","-cf","bak/backup.jar" ] + files)
	os.chdir(current)
	
	# finally copy the files to the destination
	for file in files:
		path = to_dir + file[len(from_dir):]
		dirs = path[:path.rfind("/")]
		if not os.path.exists(dirs):
			os.makedirs(dirs)
		shutil.copyfile(file,path)
	
def print_usage():
	print("\n--------------\n     USAGE\n--------------")
	print("\nThis command enables to export (resp. import) files matching a regex to (resp. from) a given Eclipse project from (resp. to) a git repository.")
	print("\nSyntax : update <operation> regex*")
	print("\n\nArgument 'operation' can be any of the following : \n  . export\n  . import")
	
def fail(msg):
	print("Failed :",msg)
	print_usage()
	exit()
	
if __name__ == '__main__':
	props = load_properties("update.ini")
	
	if sys.argv[1] == "import":
		ends = (props["eclipse-project"],props["repository"])
	elif sys.argv[1] == "export":
		ends = (props["repository"],props["eclipse-project"])
	else:
		fail(sys.argv[1] + " is not a valid operation")
	
	if len(sys.argv) < 3:
		fail("Not enough arguments.")
		
	ignore   = [ "update.ini","update.py","README.md","codemirror-4.8","lib","tmp" ]
	regs     = read_regexps([ f for f in os.listdir(ends[0]) if accept(f,ignore) ],ends[0]) if sys.argv[2] == "-all" else read_regexps(sys.argv[2:],ends[0])
	files    = fetch_files(ignore,ends[0])
	selected = filter_files(regs,files)
	copy_files(selected,ends[0],ends[1])