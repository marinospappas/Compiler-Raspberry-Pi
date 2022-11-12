###################################################################################
# input output utilities
# implements string read and write using llinux kernel calls
# to be used with tinsel output
# also integer read and write by converting string to int using atoi and vice-versa
# Author: M. Pappas
# Version 1.1 16.04.2022
###################################################################################


	.set	BUFFER_SIZE, 512

.data
	.align 8	

.bss
	.align 8
	buffer: .space BUFFER_SIZE

.text

.global	read_s_
.global	write_s_
.global	read_i_
.global	write_i_

######################
# read string function
# params:
#	rdi:	string address
#	rsi:	available string length
# returns:
#	rax:	number of bytes read
#
read_s_:
	pushq	%rbx
	pushq	%rdi
	pushq	%rsi

	# setup read call parametrs from this function's parameters
	movq	%rsi, %rdx		# string size was in rsi - must be in rdx
	movq	%rdi, %rsi		# string address was in rdi - must be in rsi
	pushq	%rsi			# save string address
	pushq	%r10			# save function parameter registers
	pushq	%r11
	xorq	%rax, %rax		# system call 0 = read
	movq	%rax, %rdi		# file descriptor 0
	syscall				# call the kernel
					# returns number of bytes read in rax
	popq	%r11			# restore function parameter registers
	popq	%r10
	popq	%rsi			# retrieve string address
    movb    $0, (%rsi,%rax)		# set string terminator

	cmp	$0, %rax		# check in case 0 characters read (ctrl-d)
	je	read_s_ret		# in that case return

	decq	%rax			# point to the last char
	cmpb	$'\n', (%rsi,%rax)	# check for newline
	jne	read_s_ret

	movb    $0, (%rsi,%rax)		# remove the newline

read_s_ret:
	popq	%rsi
	popq	%rdi
	popq	%rbx
	ret

#######################
# write string function
# params:
#	rdi:	string address
# returns:
#	rax:	number of bytes written
#
.extern	strlen_
write_s_:
	pushq	%rbx
	pushq	%rdi
	pushq	%rsi

	call	strlen_			# rdi already contains the stirng address
	movq	%rax, %rdx		# bytes to write

	movq	%rdi, %rsi		# string addess must be in rsi
	movq	$1, %rax		# system call 1 = write
	movq	%rax, %rdi		# file descriptor 1
	pushq	%r10			# save function parameter registers
	pushq	%r11
	syscall				# call the kernel

	popq	%r11			# restore function parameter registers
	popq	%r10

	popq	%rsi
	popq	%rdi
	popq	%rbx
	ret

#######################
# read integer function
# params:
#	no parameters
# returns:
#	rax: the integer
#
.extern	atoi_
read_i_:
	pushq	%rbx
	pushq	%rdi
	pushq	%rsi

	leaq	buffer(%rip), %rdi	# read string from input
	movq	$(BUFFER_SIZE-1), %rsi
	call 	read_s_

	leaq	buffer(%rip), %rdi	# convert to integer
	call	atoi_			# the integer retruned from atoi_ is already in %rax

	popq	%rsi
	popq	%rdi
	popq	%rbx
	ret

########################
# write integer function
# params:
#	rdi: the integer
# returns:
#	no return value
#
.extern	itoa_
write_i_:
	pushq	%rbx
	pushq	%rdi
	pushq	%rsi

	# interger already in rdi (first parameter)
	leaq	buffer(%rip), %rsi	# convert int to string
	call	itoa_

	leaq	buffer(%rip), %rdi	# print the string
	call	write_s_
	
	popq	%rsi
	popq	%rdi
	popq	%rbx
	ret

