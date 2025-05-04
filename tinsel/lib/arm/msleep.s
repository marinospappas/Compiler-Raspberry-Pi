	.arch armv6
	.fpu vfp
	.eabi_attribute 28, 1
	.eabi_attribute 20, 1
	.eabi_attribute 21, 1
	.eabi_attribute 23, 3
	.eabi_attribute 24, 1
	.eabi_attribute 25, 1
	.eabi_attribute 26, 2
	.eabi_attribute 30, 6
	.eabi_attribute 34, 1
	.eabi_attribute 18, 4
	.file	"msleep.c"
	.text
	.align	2
	.global	msleep
	.syntax unified
	.arm
	.type	msleep, %function
msleep:
	@ args = 0, pretend = 0, frame = 24
	@ frame_needed = 1, uses_anonymous_args = 0
	push	{fp, lr}
	add	fp, sp, #4
	sub	sp, sp, #24
	str	r0, [fp, #-24]
	ldr	r3, [fp, #-24]
	cmp	r3, #0
	bge	.L2
	bl	__errno_location
	mov	r3, r0
	mov	r2, #22
	str	r2, [r3]
	mvn	r3, #0
	b	.L6
.L2:
	ldr	r3, [fp, #-24]
	ldr	r2, .L7
	smull	r1, r2, r2, r3
	asr	r2, r2, #6
	asr	r3, r3, #31
	sub	r3, r2, r3
	str	r3, [fp, #-16]
	ldr	r1, [fp, #-24]
	ldr	r3, .L7
	smull	r2, r3, r3, r1
	asr	r2, r3, #6
	asr	r3, r1, #31
	sub	r2, r2, r3
	mov	r3, r2
	lsl	r3, r3, #5
	sub	r3, r3, r2
	lsl	r3, r3, #2
	add	r3, r3, r2
	lsl	r3, r3, #3
	sub	r2, r1, r3
	mov	r1, r2
	lsl	r1, r1, #5
	sub	r1, r1, r2
	lsl	r3, r1, #6
	sub	r3, r3, r1
	lsl	r3, r3, #3
	add	r3, r3, r2
	lsl	r3, r3, #6
	str	r3, [fp, #-12]
.L5:
	sub	r2, fp, #16
	sub	r3, fp, #16
	mov	r1, r2
	mov	r0, r3
	bl	nanosleep
	str	r0, [fp, #-8]
	ldr	r3, [fp, #-8]
	cmp	r3, #0
	beq	.L4
	bl	__errno_location
	mov	r3, r0
	ldr	r3, [r3]
	cmp	r3, #4
	beq	.L5
.L4:
	ldr	r3, [fp, #-8]
.L6:
	mov	r0, r3
	sub	sp, fp, #4
	@ sp needed
	pop	{fp, pc}
.L8:
	.align	2
.L7:
	.word	274877907
	.size	msleep, .-msleep
	.ident	"GCC: (Raspbian 12.2.0-14+rpi1) 12.2.0"
	.section	.note.GNU-stack,"",%progbits
