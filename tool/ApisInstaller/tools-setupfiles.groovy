
// Check the number of command-line arguments
if (args.length < 20) {
    println "Usage: groovy tools-setupfiles.groovy -a <apis> -e <expFile> -c <count> -sp <sshPassphrase> -i <interface> -v <version> -f <copXmlFileName> -cop \"all\"|\"first\"|<unitId> -ld <logDir> -wd <workDir>"
    System.exit(1)
}

// Command line arguments get
def apis = ""
def expFile = ""
def count = ""
def sshPassphrase = ""
def interface_ = ""
def version = ""
def copXmlFileName = ""
def cop = ""
def logDir = ""
def workDir = ""
args.eachWithIndex { item, index ->
    if (item == "-a") {
        apis = args[index + 1]
    } else if (item == "-e") {
        expFile = args[index + 1]
    } else if (item == "-c") {
        count = new Integer(args[index + 1])
    } else if (item == "-sp") {
        sshPassphrase = args[index + 1]
    } else if (item == "-i") {
        interface_ = args[index + 1]
    } else if (item == "-v") {
        version = args[index + 1]
    } else if (item == "-f") {
        copXmlFileName = args[index + 1]
    } else if (item == "-cop") {
        cop = args[index + 1].split(',')
    } else if (item == "-ld") {
        logDir = args[index + 1]
    } else if (item == "-wd") {
        workDir = args[index + 1]
    }
}

if (!cop.contains('all') && !cop.contains('first')) {
    System.exit(0)
}

// DEBUG START
println "version = ${version}"
println "copXmlFileName = ${copXmlFileName}"
println "cop = ${cop}"
println "workDir = ${workDir}"
// DEBUG END

// Read the COP information file
def copXmlFile = new File(copXmlFileName)
def community = new XmlSlurper().parse(copXmlFile)

if (community.disabledItems.disabledItem.any { it.text() == 'apis-' + apis }) {
	println 'apis-' + apis + ' is disabled'
	return
}

// Command
def java = "java -jar"
def bash = "bash"
def workDirFile = new File(workDir)
def currentDir = new File(".").getAbsolutePath()

// Execute : CopInfoTransformer
def execCommand_02 = "${java} CopInfoTransformer-${version}/CopInfoTransformer-${version}-fat.jar ${currentDir}/${copXmlFileName} ${apis} ${version} true"

// DEBUG START
println "execCommand_02 = ${execCommand_02}"
// DEBUG END

def sout_02 = new StringBuilder(), serr_02 = new StringBuilder()
def proc_02 = "${execCommand_02}".execute([], workDirFile)
proc_02.consumeProcessOutput(sout_02, serr_02)
def execResult_02 = proc_02.waitFor()

def countStr_02 = String.format("%03d", count)
new File("${logDir}/${countStr_02}-${apis}-CopInfoTransformer-out.log").text = sout_02
new File("${logDir}/${countStr_02}-${apis}-CopInfoTransformer-err.log").text = serr_02

// DEBUG START
println "execResult_02 = ${execResult_02}"
// DEBUG END

if (execResult_02 != 0) {
    println "ERROR ${execResult_02} : ${execCommand_02}"
    System.exit(1)
}

// Execute
community.firstCop.each {
    // COP information
    def hostName = it.hostName
    def account = it.account

    // DEBUG START
    println "expFile = ${expFile}"
    println "sshPassphrase = ${sshPassphrase}"
    println "interface = ${interface_}"
    println "hostName = ${hostName}"
    println "account = ${account}"
    println "version = ${version}"
    // DEBUG END

	println '##progress:apis-' + apis

    // Execute
    def execCommand = "${bash} ./${expFile} ${sshPassphrase} ${interface_} ${hostName} ${account} ${version} ${workDir}"

    // DEBUG START
    println "execCommand = ${execCommand}"
    // DEBUG END

    def sout = new StringBuilder(), serr = new StringBuilder()
    def proc = "${execCommand}".execute()
    proc.consumeProcessOutput(sout, serr)
    def execResult = proc.waitFor()

    def countStr = String.format("%03d", count)
    def expFileBasename = new File(expFile).getName()
    new File("${logDir}/${countStr}-${expFileBasename}-out.log").text = sout
    new File("${logDir}/${countStr}-${expFileBasename}-err.log").text = serr

    // DEBUG START
    println "execResult = ${execResult}"
    // DEBUG END

    if (execResult != 0) {
        println "ERROR ${execResult} : ${execCommand}"
        System.exit(1)
    }
}

System.exit(0)

