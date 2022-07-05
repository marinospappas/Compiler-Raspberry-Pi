################################################################################
# string utilities
# implements the well know C functions
# strlen, strcpy, atoi, itoa
# to be used in tinsel
# standard x86-84 call convention is used - can be tested using a C program
# author: M. Pappas
# version: 1.2 21.01.2022
################################################################################

.text

.global strlen_
.global strcpy_
.global strcat_
.global streq_
.global atoi_
.global itoa_

############################
# calculate length of string
# params:
#       rdi: address of string
# returns:
#       rax: string length
#
strlen_:
        xorq    %rax, %rax              # clear rax (will count the strlen here)
strlen_next:
        cmpb    $0, (%rdi, %rax)        # check str char in rdi[rax]
        je      strlen_ret

        inc     %rax
        jmp     strlen_next

strlen_ret:
        ret

##################################
# copy string to string
# params:
#       rdi: the destination string address
#       rsi: the source string address
# returns:
#       rax: address of destination string
#
strcpy_:
	pushq	%rcx
	xorq	%rcx, %rcx		# zero %rcx

strcpy_next:
	cmpb	$0, (%rsi, %rcx)	# check for end of string
	je	strcpy_ret

	movb	(%rsi, %rcx), %al	# get char from source
	movb	%al, (%rdi, %rcx)	# copy to destination
	inc	%rcx
	jmp	strcpy_next

strcpy_ret:
	movb	$0, (%rdi, %rcx)
	movq	%rdi, %rax		# address of dest string in %rax
	popq	%rcx
	ret
       
##################################
# concatenate string to string
# params:
#       rdi: the destination string address
#       rsi: the source string address
# returns:
#       rax: address of destination string
#
strcat_:
	pushq	%rcx
	movq	%rdi, %rcx		# save the destination address

strcat_di_end:
	cmpb	$0, (%rdi)		# fnd the end of the dest string
	je	strcat_next
	inc	%rdi
	jmp	strcat_di_end

strcat_next:
	call	strcpy_			# call our own strcpy_

	movq	%rcx, %rax		# address of dest string in %rax
	popq	%rcx
	ret

##################################
# check 2 strings for equality
# params:
#       rdi: the first string address
#       rsi: the second string address
# returns:
#       rax: 1 if equal 0 if not equal
#
streq_:
	pushq	%rcx
	pushq   %rbx
	xorq	%rcx, %rcx		# zero %rcx
	movq    $1, %rax        # set result to true

streq_next:
	movb	(%rdi, %rcx), %bl	# get char from first string
	cmp     %bl, (%rsi, %rcx)   # compare with second string
	jne     streq_ne            # not equal

	cmpb	$0, %bl     	# check for end of string
	je	streq_ret

	inc	    %rcx            # next character
	jmp	    streq_next

streq_ne:
    xorq    %rax, %rax      # set result to false

streq_ret:
	popq    %rbx
	popq	%rcx
	ret

###########################
# convert string to integer
# params:
#	rdi: address of string
# returns:
#	rax: the integer
#
atoi_:
	pushq	%rbx
	pushq	%r8

	xorq	%rax, %rax		# initialise rax to 0 - this will be our integer

	xorq	%r8, %r8		# clear r8
	movzb	(%rdi), %r8		# get the first char
	cmp	$'-', %r8		# check for negative
	je	atoi_l1

	cmp	$'+', %r8		# also check for '+'
	je	atoi_l1

	jmp	atoi_next

atoi_l1:
	inc	%rdi			# point to the next char

atoi_next:	
	movzb	(%rdi), %rbx		# get char
	cmpb	$0, %bl			# check for end of string
	je	atoi_end

	cmp	$'0', %rbx
	jl	atoi_end		# return if char is < '0'
	cmp	$'9', %rbx
	jg	atoi_end		# return if char is > '9'

	subq	$'0', %rbx		# ascii to bin value
	imulq	$10, %rax
	addq	%rbx, %rax		# x10 and add digit
	inc	%rdi			# increment pointer to next char
	jmp	atoi_next

atoi_end:
	cmp	$'-', %r8		# check whether we got a '-' as first char
	jne	atoi_ret
	neg	%rax			# and negate rax in that case

atoi_ret:
	popq	%r8
	popq	%rbx
	ret

##################################
# convert integer to string
# params:
#	rdi: the integer
#	rsi: the address of string
# returns:
#	rax: the address of string
#
itoa_:
	pushq	%rbx
	pushq	%rcx
	pushq	%rdx
	pushq	%r8

	xorq	%rcx, %rcx		# rcx is the index to the string chars
	movq	%rdi, %rax		# the interger in rax
	######## cltq				# sign extension from 32 bit to 64 bit eax -> rax
						# to be used if 32 bit integers are used
	movq	$10, %rbx
	movq	%rax, %r8		# save the integer in r8
	cmpq	$0, %rax		# check for negative
	jge	itoa_l1

	negq	%rax			# convert to positive

itoa_l1:
	xorq	%rdx, %rdx
	idivq	%rbx			# divide by 10
	addq	$'0', %rdx		# remainder in rdx - convert to ascii char
	movb	%dl, (%rsi,%rcx)
	inc	%rcx

	cmpq	$0, %rax		# check if the quotient is 0
	jne	itoa_l1

	cmpq	$0, %r8			# check for negative
	jge	itoa_end

	movb	$'-', (%rsi,%rcx)	# append '-' sign
	inc	%rcx

itoa_end:
	movb	$0, (%rsi,%rcx)		# terminate the string
	dec	%rcx			# rcx points to the last char
	xorq	%rbx, %rbx		# rbx points to the first char
itoa_rev:
	movb	(%rsi,%rbx), %dl	# reverse the string
	movb	(%rsi,%rcx), %dh
	movb	%dh, (%rsi,%rbx)
	movb	%dl, (%rsi,%rcx)

	inc	%rbx			# move to the next chars
	dec	%rcx	

	cmp	%rbx, %rcx		# check if we've reached the middle
	jg	itoa_rev
itoa_ret:
	movq	%rsi, %rax		# return the address of the string
	popq	%r8
	popq	%rdx
	popq	%rcx
	popq	%rbx
	ret

