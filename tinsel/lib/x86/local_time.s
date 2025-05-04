#
#  -- Time Module
#  -- Version 1, October 2022
#  -- Various time related functions (local time)
#  -- Recuires libc
#  -- Marinos Pappas
# 
# x86-64 Assembly Code - AT&T format
# library time
# compiled on Thu May 01 12:21:18 CEST 2025
.data
.align 8
# TINSEL version 4.0 for x86-84 (Linux) February 2025 (c) M.Pappas\n
	CLOCK_REALTIME:	.quad 0
	tv:	.space 16
.align 8
	tm:	.space 80
.align 8
	time_now:	.quad 0

.text
.align 8
.extern gettime
.extern ctime
.extern localtime_r
.extern sleep_nanosec

.global getlocaltime
# function getlocaltime
getlocaltime:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
# parameter cur_time offset from frame -8
	movq	%rax, %r10
# 	set input parameters
	call	gettime
	lea	tv(%rip), %rbx
	movq	%rax, (%rbx)
	movq	$1, %r10
	movq	%rdx, (%rbx, %r10, 8)
	movq	$0, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tv(%rip), %rax
	movq	(%rax, %rcx, 8), %rax
	testq	%rax, %rax
	movq	%rax, time_now(%rip)
	lea	time_now(%rip), %rax
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

.global getlocaltimestr
# function getlocaltimestr
getlocaltimestr:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	movq	%rax, %r10
# 	set input parameters
	call	gettime
	lea	tv(%rip), %rbx
	movq	%rax, (%rbx)
	movq	$1, %r10
	movq	%rdx, (%rbx, %r10, 8)
	movq	$0, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tv(%rip), %rax
	movq	(%rax, %rcx, 8), %rax
	testq	%rax, %rax
	movq	%rax, time_now(%rip)
	lea	time_now(%rip), %rax
# 	set input parameters
	movq	%rax, %rdi
	call	ctime
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret

.global wait_until_sec_change
# function wait_until_sec_change
wait_until_sec_change:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$8, %rsp
# local var nanosecs offset from frame -8
	subq	$8, %rsp
# local var start_usec offset from frame -16
	subq	$8, %rsp
# local var usec_now offset from frame -24
	subq	$8, %rsp
# local var usec_difference offset from frame -32
	movq	$10, %rax
	testq	%rax, %rax
	movq	%rax, -8(%rbp)
	movq	%rax, %r10
# 	set input parameters
	call	gettime
	lea	tv(%rip), %rbx
	movq	%rax, (%rbx)
	movq	$1, %r10
	movq	%rdx, (%rbx, %r10, 8)
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
wait_until_sec_change_L0_:
	movq	$1, %rax
	testq	%rax, %rax
	jz	wait_until_sec_change_L1_
	movq	%rax, %r10
# 	set input parameters
	call	gettime
	lea	tv(%rip), %rbx
	movq	%rax, (%rbx)
	movq	$1, %r10
	movq	%rdx, (%rbx, %r10, 8)
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
	jz	wait_until_sec_change_L2_
	movq	-32(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1000000, %rax
	testq	%rax, %rax
	popq	%rbx
	addq	%rbx, %rax
	movq	%rax, -32(%rbp)
wait_until_sec_change_L2_:
	movq	-32(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$0, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	setge	%al
	andq	$1, %rax
	jz	wait_until_sec_change_L3_
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret
wait_until_sec_change_L3_:
	movq	$0, %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	movq	-8(%rbp), %rax
	testq	%rax, %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	sleep_nanosec
	popq	%rbx	# restore temp param register %rbx from stack
	jmp	wait_until_sec_change_L0_
wait_until_sec_change_L1_:

.data
	.align 8

# endlibrary
