#
#  -- Time Module
#  -- Version 2, February 2025
#  -- Various time related functions
#  -- Marinos Pappas
# 
# x86-64 Assembly Code - AT&T format
# library time
# compiled on Thu Feb 20 19:26:40 CET 2025
.data
.align 8
# TINSEL version 4.0 for x86-84 (Linux) February 2025 (c) M.Pappas\n
	CLOCK_REALTIME:	.quad 0
.global UNIT_SEC
	UNIT_SEC:	.quad 2
.global UNIT_MILLISEC
	UNIT_MILLISEC:	.quad 1
.global UNIT_MICROSEC
	UNIT_MICROSEC:	.quad 0
	tm:	.space 80
.align 8
	time_epoch:	.quad 0

.text
.align 8
.extern sleep_nanosec
.extern clock_gettime

.global sleep
# function sleep
sleep:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
# parameter duration offset from frame -8
	subq	$8, %rsp
	movq	%rsi, -16(%rbp)
# parameter unit offset from frame -16
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$0, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	setl	%al
	andq	$1, %rax
	jz	sleep_L0_
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret
sleep_L0_:
	movq	-16(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	UNIT_MILLISEC(%rip), %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	sleep_L1_
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$999, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	setg	%al
	andq	$1, %rax
	jz	sleep_L2_
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	cqto		# sign extend to rdx
	idivq	%rbx, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	movq	$0, %rax
	testq	%rax, %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	sleep_nanosec
	popq	%rbx	# restore temp param register %rbx from stack
	movq	$0, %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	cqto		# sign extend to rdx
	idivq	%rbx, %rax
	movq	%rdx, %rax
	pushq	%rax
	movq	$1000000, %rax
	testq	%rax, %rax
	popq	%rbx
	imulq	%rbx, %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	sleep_nanosec
	popq	%rbx	# restore temp param register %rbx from stack
	jmp	sleep_L3_
sleep_L2_:
	movq	$0, %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000000, %rax
	testq	%rax, %rax
	popq	%rbx
	imulq	%rbx, %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	sleep_nanosec
	popq	%rbx	# restore temp param register %rbx from stack
sleep_L3_:
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret
sleep_L1_:
	movq	-16(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	UNIT_MICROSEC(%rip), %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	sleep_L4_
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$999999, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	setg	%al
	andq	$1, %rax
	jz	sleep_L5_
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000000, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	cqto		# sign extend to rdx
	idivq	%rbx, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	movq	$0, %rax
	testq	%rax, %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	sleep_nanosec
	popq	%rbx	# restore temp param register %rbx from stack
	movq	$0, %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000000, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	cqto		# sign extend to rdx
	idivq	%rbx, %rax
	movq	%rdx, %rax
	pushq	%rax
	movq	$1000, %rax
	testq	%rax, %rax
	popq	%rbx
	imulq	%rbx, %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	sleep_nanosec
	popq	%rbx	# restore temp param register %rbx from stack
	jmp	sleep_L6_
sleep_L5_:
	movq	$0, %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000, %rax
	testq	%rax, %rax
	popq	%rbx
	imulq	%rbx, %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	sleep_nanosec
	popq	%rbx	# restore temp param register %rbx from stack
sleep_L6_:
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret
sleep_L4_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	movq	$0, %rax
	testq	%rax, %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	sleep_nanosec
	popq	%rbx	# restore temp param register %rbx from stack
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret

.global gettime__
# function gettime__
gettime__:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
# parameter time_now offset from frame -8
	movq	CLOCK_REALTIME(%rip), %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	lea	-8(%rbp), %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	clock_gettime
	popq	%rbx	# restore temp param register %rbx from stack
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret

.data
	.align 8

# endlibrary
