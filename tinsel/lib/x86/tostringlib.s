# x86-64 Assembly Code - AT&T format
# library int2string
# compiled on Sat Feb 15 16:47:07 CET 2025
.data
.align 8
	tinsel_msg_: .string "TINSEL version 3.2 for x86-84 (Linux) November 2022 (c) M.Pappas\n"
	newline_: .string "\n"
.align 8
	buffer:	.space 16
.align 8

.text
.align 8

.global digit2ascii
# function digit2ascii
digit2ascii:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
# parameter i offset from frame -8
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$48, %rax
	testq	%rax, %rax
	popq	%rbx
	addq	%rbx, %rax
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret

.global int2string
# function int2string
int2string:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
# parameter n offset from frame -8
	subq	$8, %rsp
	movq	%rsi, -16(%rbp)
# parameter s offset from frame -16
	subq	$8, %rsp
	movq	$0, -24(%rbp)
# local var len offset from frame -24
	subq	$8, %rsp
# local var number offset from frame -32
	subq	$8, %rsp
# local var digit offset from frame -40
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	movq	%rax, -32(%rbp)
int2string_L0_:
	movq	-32(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$10, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	cqto		# sign extend to rdx
	idivq	%rbx, %rax
	movq	%rdx, %rax
	movq	%rax, -40(%rbp)
	movq	-32(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$10, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	cqto		# sign extend to rdx
	idivq	%rbx, %rax
	movq	%rax, -32(%rbp)
	movq	-24(%rbp), %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	-40(%rbp), %rax
	testq	%rax, %rax
	movb	%al, %bl
	lea	buffer(%rip), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	-24(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1, %rax
	testq	%rax, %rax
	popq	%rbx
	addq	%rbx, %rax
	movq	%rax, -24(%rbp)
	movq	-32(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$0, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	int2string_L0_
int2string_L1_:
	subq	$8, %rsp
	movq	$0, %rax
	testq	%rax, %rax
	movq	%rax, -48(%rbp)
	subq	$8, %rsp
	movq	-24(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	subq	%rbx, %rax
	movq	%rax, -56(%rbp)
	movq	-48(%rbp), %rax
	testq	%rax, %rax
	decq	%rax
	movq	%rax, -48(%rbp)
int2string_L2_:
	movq	-48(%rbp), %rax
	testq	%rax, %rax
	incq	%rax
	movq	%rax, -48(%rbp)
	pushq	%rax
	movq	-56(%rbp), %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	setle	%al
	andq	$1, %rax
	jz	int2string_L3_
	movq	-48(%rbp), %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	-24(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	-48(%rbp), %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	subq	%rbx, %rax
	pushq	%rax
	movq	$1, %rax
	testq	%rax, %rax
	movq	%rax, %rbx
	popq	%rax
	subq	%rbx, %rax
	movq	%rax, %rcx
	lea	buffer(%rip), %rax
	movb	(%rax, %rcx, 1), %al
	andq	$0xFF, %rax
	pushq	%rax
	movq	$48, %rax
	testq	%rax, %rax
	popq	%rbx
	addq	%rbx, %rax
	movb	%al, %bl
	movq	-16(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	jmp	int2string_L2_
int2string_L3_:
	addq	$16, %rsp
	movq	-24(%rbp), %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	$0, %rax
	testq	%rax, %rax
	movb	%al, %bl
	movq	-16(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret

.global month2string
# function month2string
month2string:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
# parameter month offset from frame -8
	subq	$8, %rsp
	movq	%rsi, -16(%rbp)
# parameter s offset from frame -16
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$1, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	month2string_L0_
	lea	STRCNST_1(%rip), %rax
	movq	%rax, %rsi		# assign string - strcpy_(offset(%rbp), %rax)
	movq	-16(%rbp), %rdi
	call	strcpy_
month2string_L0_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$2, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	month2string_L1_
	lea	STRCNST_2(%rip), %rax
	movq	%rax, %rsi		# assign string - strcpy_(offset(%rbp), %rax)
	movq	-16(%rbp), %rdi
	call	strcpy_
month2string_L1_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$3, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	month2string_L2_
	lea	STRCNST_3(%rip), %rax
	movq	%rax, %rsi		# assign string - strcpy_(offset(%rbp), %rax)
	movq	-16(%rbp), %rdi
	call	strcpy_
month2string_L2_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$4, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	month2string_L3_
	lea	STRCNST_4(%rip), %rax
	movq	%rax, %rsi		# assign string - strcpy_(offset(%rbp), %rax)
	movq	-16(%rbp), %rdi
	call	strcpy_
month2string_L3_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$5, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	month2string_L4_
	lea	STRCNST_5(%rip), %rax
	movq	%rax, %rsi		# assign string - strcpy_(offset(%rbp), %rax)
	movq	-16(%rbp), %rdi
	call	strcpy_
month2string_L4_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$6, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	month2string_L5_
	lea	STRCNST_6(%rip), %rax
	movq	%rax, %rsi		# assign string - strcpy_(offset(%rbp), %rax)
	movq	-16(%rbp), %rdi
	call	strcpy_
month2string_L5_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$7, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	month2string_L6_
	lea	STRCNST_7(%rip), %rax
	movq	%rax, %rsi		# assign string - strcpy_(offset(%rbp), %rax)
	movq	-16(%rbp), %rdi
	call	strcpy_
month2string_L6_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$8, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	month2string_L7_
	lea	STRCNST_8(%rip), %rax
	movq	%rax, %rsi		# assign string - strcpy_(offset(%rbp), %rax)
	movq	-16(%rbp), %rdi
	call	strcpy_
month2string_L7_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$9, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	month2string_L8_
	lea	STRCNST_9(%rip), %rax
	movq	%rax, %rsi		# assign string - strcpy_(offset(%rbp), %rax)
	movq	-16(%rbp), %rdi
	call	strcpy_
month2string_L8_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$10, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	month2string_L9_
	lea	STRCNST_10(%rip), %rax
	movq	%rax, %rsi		# assign string - strcpy_(offset(%rbp), %rax)
	movq	-16(%rbp), %rdi
	call	strcpy_
month2string_L9_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$11, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	month2string_L10_
	lea	STRCNST_11(%rip), %rax
	movq	%rax, %rsi		# assign string - strcpy_(offset(%rbp), %rax)
	movq	-16(%rbp), %rdi
	call	strcpy_
month2string_L10_:
	movq	-8(%rbp), %rax
	testq	%rax, %rax
	pushq	%rax
	movq	$12, %rax
	testq	%rax, %rax
	popq	%rbx
	cmp	%rax, %rbx
	sete	%al
	andq	$1, %rax
	jz	month2string_L11_
	lea	STRCNST_12(%rip), %rax
	movq	%rax, %rsi		# assign string - strcpy_(offset(%rbp), %rax)
	movq	-16(%rbp), %rdi
	call	strcpy_
month2string_L11_:
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret

.data
	.align 8
# constant string values go here
	STRCNST_1:	.string "jan"
.align 8
	STRCNST_2:	.string "feb"
.align 8
	STRCNST_3:	.string "mar"
.align 8
	STRCNST_4:	.string "apr"
.align 8
	STRCNST_5:	.string "may"
.align 8
	STRCNST_6:	.string "jun"
.align 8
	STRCNST_7:	.string "jul"
.align 8
	STRCNST_8:	.string "aug"
.align 8
	STRCNST_9:	.string "sep"
.align 8
	STRCNST_10:	.string "oct"
.align 8
	STRCNST_11:	.string "nov"
.align 8
	STRCNST_12:	.string "dec"
.align 8

# end endlibrary
