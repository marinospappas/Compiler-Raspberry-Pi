#
# *-- Time Module
# *-- Version 1, October 2022
# *-- Various time related functions
# *-- Marinos Pappas
# 
# x86-64 Assembly Code - AT&T format
# library time
# compiled on Sat Feb 15 16:45:34 CET 2025

.text

.global getlocaltime
.global getlocaltimestr
.global timeout

# function getlocaltime
getlocaltime:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
# parameter cur_time offset from frame -8
	lea	time_epoch(%rip), %rax
# 	set input parameters
	movq	%rax, %rdi
	call	time
	lea	time_epoch(%rip), %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	lea	tm(%rip), %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	localtime_r
	popq	%rbx	# restore temp param register %rbx from stack
	movq	$0, %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	$8, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tm(%rip), %rax
	movb	(%rax, %rcx, 1), %al
	andq	$0xFF, %rax
	movb	%al, %bl
	movq	-8(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	$1, %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	$4, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tm(%rip), %rax
	movb	(%rax, %rcx, 1), %al
	andq	$0xFF, %rax
	movb	%al, %bl
	movq	-8(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	$2, %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	$0, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tm(%rip), %rax
	movb	(%rax, %rcx, 1), %al
	andq	$0xFF, %rax
	movb	%al, %bl
	movq	-8(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	$3, %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	$12, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tm(%rip), %rax
	movb	(%rax, %rcx, 1), %al
	andq	$0xFF, %rax
	movb	%al, %bl
	movq	-8(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	$4, %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	$16, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tm(%rip), %rax
	movb	(%rax, %rcx, 1), %al
	andq	$0xFF, %rax
	pushq	%rax
	movq	$1, %rax
	testq	%rax, %rax
	popq	%rbx
	addq	%rbx, %rax
	movb	%al, %bl
	movq	-8(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	$5, %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	$20, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tm(%rip), %rax
	movb	(%rax, %rcx, 1), %al
	andq	$0xFF, %rax
	movb	%al, %bl
	movq	-8(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret

# function timeout_sec
timeout_sec:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
# parameter duration offset from frame -8
	subq	$8, %rsp
# local var start_time offset from frame -16
	subq	$8, %rsp
# local var start_nsec offset from frame -24
	subq	$8, %rsp
# local var time_now offset from frame -32
	subq	$8, %rsp
# local var nsec_now offset from frame -40
	subq	$8, %rsp
# local var sec_difference offset from frame -48
	subq	$8, %rsp
# local var nsec_difference offset from frame -56
	movq	CLOCK_REALTIME(%rip), %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	lea	tv(%rip), %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	clock_gettime
	popq	%rbx	# restore temp param register %rbx from stack
	movq	$0, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tv(%rip), %rax
	movq	(%rax, %rcx, 8), %rax
	testq	%rax, %rax
	movq	%rax, -16(%rbp)
	movq	$1, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tv(%rip), %rax
	movq	(%rax, %rcx, 8), %rax
	testq	%rax, %rax
	movq	%rax, -24(%rbp)
timeout_sec_L0_:
	movq	$1, %rax
	testq	%rax, %rax
	jz	timeout_sec_L1_
	movq	CLOCK_REALTIME(%rip), %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	lea	tv(%rip), %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	clock_gettime
	popq	%rbx	# restore temp param register %rbx from stack
	movq	$0, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tv(%rip), %rax
	movq	(%rax, %rcx, 8), %rax
	testq	%rax, %rax
	movq	%rax, -32(%rbp)
	movq	$1, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tv(%rip), %rax
	movq	(%rax, %rcx, 8), %rax
	testq	%rax, %rax
	movq	%rax, -40(%rbp)
	movq	-32(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	-16(%rbp), %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	subq	%rbx, %rax
	movq	%rax, -48(%rbp)
	movq	-40(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	-24(%rbp), %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	subq	%rbx, %rax
	movq	%rax, -56(%rbp)
	movq	-48(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	setge	%al
	andq	$1, %rax
	jz	timeout_sec_L2_
	movq	-56(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$0, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	setg	%al
	andq	$1, %rax
	jz	timeout_sec_L3_
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret
timeout_sec_L3_:
timeout_sec_L2_:
	jmp	timeout_sec_L0_
timeout_sec_L1_:

# function timeout_millisec
timeout_millisec:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
# parameter duration offset from frame -8
	subq	$8, %rsp
# local var start_msec offset from frame -16
	subq	$8, %rsp
# local var msec_now offset from frame -24
	subq	$8, %rsp
# local var msec_difference offset from frame -32
	movq	CLOCK_REALTIME(%rip), %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	lea	tv(%rip), %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	clock_gettime
	popq	%rbx	# restore temp param register %rbx from stack
	movq	$1, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tv(%rip), %rax
	movq	(%rax, %rcx, 8), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000000, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	cqto		# sign extend to rdx
	idivq	%rbx, %rax
	movq	%rax, -16(%rbp)
timeout_millisec_L0_:
	movq	$1, %rax
	testq	%rax, %rax
	jz	timeout_millisec_L1_
	movq	CLOCK_REALTIME(%rip), %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	lea	tv(%rip), %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	clock_gettime
	popq	%rbx	# restore temp param register %rbx from stack
	movq	$1, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tv(%rip), %rax
	movq	(%rax, %rcx, 8), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000000, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	cqto		# sign extend to rdx
	idivq	%rbx, %rax
	movq	%rax, -24(%rbp)
	movq	-24(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	-16(%rbp), %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	subq	%rbx, %rax
	movq	%rax, -32(%rbp)
	movq	-32(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$0, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	setl	%al
	andq	$1, %rax
	jz	timeout_millisec_L2_
	movq	-32(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000, %rax
	testq	%rax, %rax
	popq	%rbx
	addq	%rbx, %rax
	movq	%rax, -32(%rbp)
timeout_millisec_L2_:
	movq	-32(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	setge	%al
	andq	$1, %rax
	jz	timeout_millisec_L3_
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret
timeout_millisec_L3_:
	jmp	timeout_millisec_L0_
timeout_millisec_L1_:

# function timeout_microsec
timeout_microsec:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
# parameter duration offset from frame -8
	subq	$8, %rsp
# local var start_usec offset from frame -16
	subq	$8, %rsp
# local var usec_now offset from frame -24
	subq	$8, %rsp
# local var usec_difference offset from frame -32
	movq	CLOCK_REALTIME(%rip), %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	lea	tv(%rip), %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	clock_gettime
	popq	%rbx	# restore temp param register %rbx from stack
	movq	$1, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tv(%rip), %rax
	movq	(%rax, %rcx, 8), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	cqto		# sign extend to rdx
	idivq	%rbx, %rax
	movq	%rax, -16(%rbp)
timeout_microsec_L0_:
	movq	$1, %rax
	testq	%rax, %rax
	jz	timeout_microsec_L1_
	movq	CLOCK_REALTIME(%rip), %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	lea	tv(%rip), %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	clock_gettime
	popq	%rbx	# restore temp param register %rbx from stack
	movq	$1, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tv(%rip), %rax
	movq	(%rax, %rcx, 8), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	cqto		# sign extend to rdx
	idivq	%rbx, %rax
	movq	%rax, -24(%rbp)
	movq	-24(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	-16(%rbp), %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	subq	%rbx, %rax
	movq	%rax, -32(%rbp)
	movq	-32(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$0, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	setl	%al
	andq	$1, %rax
	jz	timeout_microsec_L2_
	movq	-32(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000000, %rax
	testq	%rax, %rax
	popq	%rbx
	addq	%rbx, %rax
	movq	%rax, -32(%rbp)
timeout_microsec_L2_:
	movq	-32(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	setge	%al
	andq	$1, %rax
	jz	timeout_microsec_L3_
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret
timeout_microsec_L3_:
	jmp	timeout_microsec_L0_
timeout_microsec_L1_:

.global timeout
# function timeout
timeout:
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
	jz	timeout_L0_
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret
timeout_L0_:
	movq	-16(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	UNIT_MILLISEC(%rip), %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	timeout_L1_
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$999, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	setg	%al
	andq	$1, %rax
	jz	timeout_L2_
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	cqto		# sign extend to rdx
	idivq	%rbx, %rax
# 	set input parameters
	movq	%rax, %rdi
	call	timeout_sec
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
# 	set input parameters
	movq	%rax, %rdi
	call	timeout_millisec
	jmp	timeout_L3_
timeout_L2_:
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
# 	set input parameters
	movq	%rax, %rdi
	call	timeout_millisec
timeout_L3_:
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret
timeout_L1_:
	movq	-16(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	UNIT_MICROSEC(%rip), %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	timeout_L4_
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$999999, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	setg	%al
	andq	$1, %rax
	jz	timeout_L5_
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000000, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	cqto		# sign extend to rdx
	idivq	%rbx, %rax
# 	set input parameters
	movq	%rax, %rdi
	call	timeout_sec
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
# 	set input parameters
	movq	%rax, %rdi
	call	timeout_microsec
	jmp	timeout_L6_
timeout_L5_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
# 	set input parameters
	movq	%rax, %rdi
	call	timeout_microsec
timeout_L6_:
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret
timeout_L4_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
# 	set input parameters
	movq	%rax, %rdi
	call	timeout_sec
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret

.global getlocaltimestr
# function getlocaltimestr
getlocaltimestr:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	lea	time_epoch(%rip), %rax
# 	set input parameters
	movq	%rax, %rdi
	call	time
	lea	time_epoch(%rip), %rax
# 	set input parameters
	movq	%rax, %rdi
	call	ctime
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret

.data
	.align 8

# end endlibrary
