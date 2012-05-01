# Nathaniel Lim 12/10/2010 CS336T

#Create a simulator object
set ns [new Simulator]

#Open the nam trace file
set nf [open out.nam w]
$ns trace-all $nf
$ns namtrace-all $nf

#Define a 'finish' procedure
proc finish {} {
        global ns nf
        $ns flush-trace
	#Close the trace file
        close $nf
	#Execute nam on the trace file
        exec nam out.nam &
        exit 0
}

#Create two nodes

set jalama	[$ns node]
set balboa	[$ns node]
set paloma	[$ns node] 
set condesa	[$ns node]
set alameda	[$ns node]
set roqueta	[$ns node]
set montara	[$ns node]

$jalama shape circle
$balboa shape box
$paloma shape circle
$condesa shape box
$alameda shape box
$montara shape circle
$roqueta shape circle

$ns duplex-link $jalama $balboa 10Mb 17ms DropTail
$ns duplex-link $paloma $balboa 10Mb 1ms DropTail
$ns duplex-link $balboa $condesa 800kb 1ms DropTail
$ns duplex-link $condesa $alameda 800kb 1ms DropTail
$ns duplex-link $alameda $roqueta 10Mb 1ms DropTail
$ns duplex-link $alameda $montara 10Mb 1ms DropTail

$ns duplex-link-op $jalama $balboa queuePos -0.5
$ns duplex-link-op $paloma $balboa queuePos -0.5
$ns duplex-link-op $balboa $condesa queuePos -0.5
$ns duplex-link-op $condesa $alameda queuePos -0.5
$ns duplex-link-op $alameda $roqueta queuePos -0.5
$ns duplex-link-op $alameda $montara queuePos -0.5

$ns duplex-link-op $jalama $balboa orient right-up
$ns duplex-link-op $paloma $balboa orient right-down
$ns duplex-link-op $balboa $condesa orient right
$ns duplex-link-op $condesa $alameda orient right
$ns duplex-link-op $alameda $roqueta orient right-up
$ns duplex-link-op $alameda $montara orient right-down

$ns queue-limit $balboa $condesa 12


#$ns duplex-link-op $balboa $condesa -l "800kbps, 25ms"
#$ns duplex-link-op $condesa $alameda -l "800kbps, 25ms"


$paloma label "paloma"
$jalama label "jalama"
$balboa label "balboa"
$condesa label "condesa"
$alameda label "alameda"
$montara label "montara"
$roqueta label "roqueta"

set tcp1 [new Agent/TCP]
#set tcp2 [new Agent/TCP/Vegas]
set tcp2 [new Agent/TCP]
set tcpsink1 [new Agent/TCPSink]
set tcpsink2 [new Agent/TCPSink]

$ns attach-agent $jalama   $tcp1
$ns attach-agent $roqueta  $tcpsink1

$ns attach-agent $paloma   $tcp2
$ns attach-agent $montara  $tcpsink2

$ns connect $tcp1 $tcpsink1
$ns connect $tcp2 $tcpsink2

$tcp1 set fid_ 1
$tcp2 set fid_ 2

$ns color 1 Blue
$ns color 2 Red

#Setup FTP on each TCP Agents
set ftp1 [new Application/FTP]
set ftp2 [new Application/FTP]

$ftp1 attach-agent $tcp1
$ftp2 attach-agent $tcp2

$ns at 0.0 "$ftp1 produce 300"
$ns at 0.0 "$ftp2 produce 300"
#$ns at 5.0 "$ftp1 stop"
#$ns at 5.0 "$ftp2 stop"
$ns at 50.5 "finish"

#Run the simulation
puts "Starting Simulation"
$ns run
