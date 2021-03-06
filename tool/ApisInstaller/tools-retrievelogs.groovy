
// Check the number of command-line arguments
if (args.length < 20) {
    println "Usage: groovy tools-retrievelogs.groovy -a <apis> -e <expFile> -c <count> -sp <sshPassphrase> -i <interface> -v <version> -f <copXmlFileName> -cop \"all\"|\"first\"|<unitId> -ld <logDir> -dp <destinationPath>"
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
def destinationPath = ""
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
    } else if (item == "-dp") {
        destinationPath = args[index + 1]
    }
}

if (!cop.contains('all') && !cop.contains('first')) {
    System.exit(0)
}

// DEBUG START
println "version = ${version}"
println "copXmlFileName = ${copXmlFileName}"
println "cop = ${cop}"
// DEBUG END

// Command
def bash = "bash"

// Read the COP information file
def copXmlFile = new File(copXmlFileName)
def community = new XmlSlurper().parse(copXmlFile)

if (community.disabledItems.disabledItem.any { it.text() == 'apis-' + apis }) {
	println 'apis-' + apis + ' is disabled'
	return
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
    println "destinationPath = ${destinationPath}"
    // DEBUG END

	println '##progress:apis-' + apis

    // Execute
    def execCommand = "${bash} ./${expFile} ${sshPassphrase} ${interface_} ${hostName} ${account} ${version} ${destinationPath}"

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

