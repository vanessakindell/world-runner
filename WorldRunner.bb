AppTitle "The 3D Battles of World Runner"

Global GraphicX#
Global GraphicY#
Global GraphicDepth#
Global GraphicMode#

; Includes

Include "Source/System.bb"
Include "Source/GameFuncs.bb"

SetupResolution()

Cls
Text GraphicsWidth()/2,GraphicsHeight()/2,"Loading...",1,1
Flip

; Load loading screen (logo)
Global GFXLogo = LoadImage("Data/Logo.png")
ResizeImage GFXLogo,GraphicsWidth(),GraphicsHeight()

; Show loading screen (logo)
Cls
DrawImage GFXLogo,0,0
Flip

; Setup scancode conversions
Restore ScanCodesToAscii_Data
While Indx <> 999
	Read Indx
	If indx <> 999 Then
		Read ascii
		Read scn	
		ac.ASCII_Convert = New ASCII_Convert
			ac\ascii=ascii
			ac\scn=scn
			ac\Indx=Indx
	EndIf
Wend

; Default Key Controls Globals
Global Key_Jump = 57
Global Key_Shoot = 29
Global Key_SpeedUp = 200
Global Key_Left = 203
Global Key_Right = 205
Global Key_Pause = 1

; Load controls/change controls
SetupControls()

FlushKeys()

; Since everything we load after this point is in the data folder
; Switch the directory to data for convienance
ChangeDir "Data/"

; Show loading screen (logo) again
Cls
DrawImage GFXLogo,0,0
Flip

; We won't be using this again so free it from memory
FreeImage GFXLogo

;Sounds
Global SFXJumpUp = LoadSound("JumpUp.Wav")
Global SFXLand = LoadSound("Land.Wav")
Global SFXExplode = LoadSound("explode.wav")
Global SFXGameOver = LoadSound("GameOver.mp3")
Global SFXLifeLost = LoadSound("LifeLost.mp3")
Global SFXCollide = LoadSound("Collide.wav")
Global SFXFire = LoadSound("Fire.wav")
Global SFXDing = LoadSound("Ding.wav")
Global SFXPickup = LoadSound("Pickup.wav")
Global SFXStar = LoadSound("Star.wav")

; Initilize Globals
Global Jump#
Global Lives#=3
Global BGM
Global Health#=1
Global Top
Global HasGun=False
Global ReverseTimer#
Global DingCount=10
Global CurrentLevel=1
Global Score#=0

; Loads the background to be shown behind the world
Global GFX_Background = LoadImage("Background.png")
ResizeImage GFX_Background,GraphicsWidth(),GraphicsHeight()/1.5

;Setup Collisions
Collisions 2,1,2,1

; --Start loading 3D objects--

; Creates the texture for the ground
Groundtex=CreateTexture(100,100)
SetBuffer(TextureBuffer(Groundtex))
Color 252,216,168
Rect 0,0,130,130
Color 255,152,56
Rect 0,0,65,65
Rect 65,65,65,65
Color 255,255,255
SetBuffer(BackBuffer())

; Create the ground
Global Ground=CreatePlane()
EntityOrder Ground,2
EntityTexture Ground,Groundtex
ScaleTexture groundtex,100,100

; Create an eye for the players to see with
Global camera=CreateCamera()
PositionEntity camera,0,15,0
CameraClsColor camera,60,188,252
CameraClsMode Camera,0,1

; Setup a nice light for the world so we can see it properly
AmbientLight 255,255,255

; Load Jack's sprites
Global Jack=LoadAnimImage("Jack.Png",74,112,0,8)
MaskImage Jack,255,201,14
MidHandle Jack
Global Pain=LoadImage("Pain.Png")
MaskImage Pain,255,201,14
MidHandle Pain

; Load the sprite for the sparks that fly from killed meanies
Global spark=LoadSprite( "spark.bmp" )
EntityAlpha spark, 0

; Cap Frames Per Second, Don't change this unless you want to bollocks up all the animations
Global FrameTimer = CreateTimer(30)

; Setup all the types
Type Meanie
	Field Entity,Movement,timer,shake,jitter,tex
End Type

Type frags
	Field speed#, entity, alpha#
End Type

Type ASCII_Convert
	Field Indx,scn,ascii
End Type

Type pit
	Field entity,size,ID
End Type

Type Pipe
	Field entity,contents,height
End Type

Type Star
	Field entity
End Type

Type Pillar
	Field entity
End Type

Type Bullet
	Field Entity,life
End Type

Type Powerup
	Field Entity,Form
End Type

; Load the texture applied to Meanies
Global MeanieTex=LoadBrush("Meanie.png")
Global OldMeanieTex=LoadBrush("OldMeanie.png")

; --Generate Level--

; First we place the meanies all over the map
For count = 1 To 100
	M.Meanie = New Meanie
	M\Entity = CreateSphere()
	EntityType M\Entity,1
	EntityShininess M\Entity,0
	ScaleEntity M\Entity,3,3,3
	PaintEntity M\Entity,MeanieTex
	PositionEntity M\Entity,Rand(-1000,1000),3,Rand(400,4000)
	M\Movement=Rand(0,1)
	M\Timer=Rand(0,120)
Next

; Next the bonus point stars
For Count = 1 To 100
	St.Star = New Star
	St\entity = LoadSprite("Star.png",7)
	ScaleSprite St\Entity,3,3
	ScaleEntity St\entity,3,3,3
	PositionEntity St\Entity,Rand(-1000,1000),3,Rand(5000,8000)
Next

; Now the pillars of fire
PillarTex = LoadTexture("Pillar.Png")
TextureFilter PillarTex,7
For Count = 1 To 100
	Pl.Pillar = New Pillar
	Pl\entity = CreateCylinder()
	EntityType Pl\Entity,1	
	EntityTexture Pl\entity,PillarTex
	ScaleEntity Pl\entity,5,15,5
	PositionEntity Pl\Entity,Rand(-1000,1000),15,Rand(5000,8000)
	RotateEntity Pl\entity,0,90,0
Next

; Now for all those powerup pipes
PipeTex = LoadTexture("Pipe.Png")
For Count = 1 To 90
	Pp.Pipe = New Pipe
	Pp\entity = CreateCylinder()
	EntityType Pp\Entity,1	
	EntityTexture pp\entity,PipeTex
	ScaleEntity pp\entity,5,15,5	; These are tall
	PositionEntity pp\entity,(Count-50)*100,15,200
	RotateEntity pp\entity,0,90,0	;Make it face the camera
	pp\contents=1	;These contain the rocket guns
	pp\height=15	; These are tall, let the rest of the program know
Next

For Count = 1 To 90
	Pp.Pipe = New Pipe
	Pp\entity = CreateCylinder()
	EntityType Pp\Entity,1
	EntityTexture pp\entity,PipeTex
	ScaleEntity pp\entity,5,10,5	;These are medium height
	PositionEntity pp\entity,(Count-50)*100,10,300
	RotateEntity pp\entity,0,90,0	;Make it face the camera
	pp\contents=2	; These contain the health potions
	pp\height=10	; These are medium height, let the rest of the program know
Next


For Count = 1 To 90
	Pp.Pipe = New Pipe
	Pp\entity = CreateCylinder()
	EntityType Pp\Entity,1
	EntityTexture pp\entity,PipeTex
	ScaleEntity pp\entity,5,5,5		;These are short
	PositionEntity pp\entity,(Count-50)*100,5,400
	RotateEntity pp\entity,0,90,0	;Make it face the camera
	pp\contents=3	; These contain the poison mushrooms OUCH!
	pp\height=5		; These are short, let the rest of the program know
Next

; This creates the pits of death
oldlok=600
For count = 1 To 20
	p.pit = New pit
	p\id=count
	p\entity = CreateCube()
	EntityOrder p\entity,1
	EntityColor p\entity,0,0,0
	place=Rand(250,500)
	p\size=Rand(40,100)
	PositionEntity p\entity,0,0,oldlok+place+p\size
	oldlok=oldlok+place+p\size
	ScaleEntity p\entity,4000,.01,p\size
Next

; This creates more pits of death
oldlok=10600
For count = 1 To 20
	p.pit = New pit
	p\id=count
	p\entity = CreateCube()
	EntityOrder p\entity,1
	EntityColor p\entity,0,0,0
	place=Rand(250,500)
	p\size=Rand(40,100)
	PositionEntity p\entity,0,0,oldlok+place+p\size
	oldlok=oldlok+place+p\size
	ScaleEntity p\entity,4000,.01,p\size
Next

; This creates even more pits of death
oldlok=20600
For count = 1 To 20
	p.pit = New pit
	p\id=count
	p\entity = CreateCube()
	EntityOrder p\entity,1
	EntityColor p\entity,0,0,0
	place=Rand(250,500)
	p\size=Rand(40,100)
	PositionEntity p\entity,0,0,oldlok+place+p\size
	oldlok=oldlok+place+p\size
	ScaleEntity p\entity,4000,.01,p\size
Next

; This creates yet more pits of death
oldlok=30600
For count = 1 To 20
	p.pit = New pit
	p\id=count
	p\entity = CreateCube()
	EntityOrder p\entity,1
	EntityColor p\entity,0,0,0
	place=Rand(250,500)
	p\size=Rand(40,100)
	PositionEntity p\entity,0,0,oldlok+place+p\size
	oldlok=oldlok+place+p\size
	ScaleEntity p\entity,4000,.01,p\size
Next

; Yes, there are still more
oldlok=40600
For count = 1 To 20
	p.pit = New pit
	p\id=count
	p\entity = CreateCube()
	EntityOrder p\entity,1
	EntityColor p\entity,0,0,0
	place=Rand(250,500)
	p\size=Rand(40,100)
	PositionEntity p\entity,0,0,oldlok+place+p\size
	oldlok=oldlok+place+p\size
	ScaleEntity p\entity,4000,.01,p\size
Next

; This creates the 3D locator for Jack
Global JackLocator = CreateSphere()
ScaleEntity JackLocator,2,2.5,1
EntityAlpha JackLocator,0
PositionEntity JackLocator,EntityX(camera),EntityY(Camera)-12.5,EntityZ(Camera)+30

; This puts a shadow underneath so players can see where they are when jumping
Global shadow=CreateSphere(8,Camera)
MoveEntity shadow,0,-15,30
ScaleEntity shadow,1.5,.2,1
EntityColor shadow,0,0,0
EntityAlpha shadow,0	; Jack isn't there yet so don't show it yet

; We're ready, let's bring Jack to life
Start()

; Gotta setup these variables
HasGun=False
GunCooldown=0

; --Main Loop--
While Not leaveitplease
	Cls
	If KeyDown(Key_SpeedUp) Then
		If CurrentLevel<4 Then
			Speed#=8
		Else
			Speed#=3+CurrentLevel
		EndIf
	Else
		Speed#=3+CurrentLevel
	EndIf
	If KeyDown(Key_Left) Then TranslateEntity camera,-4,0,0
	If KeyDown(Key_Right) Then TranslateEntity camera,4,0,0
	
	If ReverseTimer#>0 Then
		ReverseTimer#=ReverseTimer#-1
		Speed#=-4
	EndIf
	
	TranslateEntity camera,0,0,Speed#
	
	GunCooldown=GunCooldown-1
	If HasGun And KeyDown(Key_Shoot)  And GunCooldown<0 Then
		FireBullet()
		PlaySound(SFXFire)
		GunCooldown=15
	EndIf
	
	UpdatePipes()	
	UpdateMeanies()
	updateparticles()
	UpdatePowerups()
	UpdateBullets()
	UpdatePillars()
	UpdatePits()
	UpdateStars()
	
	If EntityX(camera)<-1000 Then
		MoveEntity Camera,2000,0,0
	EndIf

	If EntityX(camera)>1000 Then
		MoveEntity Camera,-2000,0,0
	EndIf
	
	TileImage GFX_Background,EntityX(Camera)/1000,0

	PositionEntity JackLocator,EntityX(camera),Jump#+EntityY(Camera)-12.5,EntityZ(Camera)+30

	UpdateWorld
	RenderWorld
	
	If KeyHit(Key_Pause) Then
		Delay 500
		FlushKeys()
		fart=False
		While Not fart
			Cls
			TileImage GFX_Background,EntityX(Camera)/1000,0
			UpdateWorld
			RenderWorld
			Color 0,0,0
			Rect (GraphicsWidth()/2)-(StringWidth("GAME PAUSED. Press pause again to exit, jump to resume.")/2),(GraphicsHeight()/2)-(FontHeight()/2),StringWidth("GAME PAUSED. Press pause again to exit, jump to resume."),FontHeight(),1
			Color 255,255,255
			Text GraphicsWidth()/2,GraphicsHeight()/2,"GAME PAUSED. Press pause again to exit, jump to resume.",1,1
			Flip
			If KeyHit(Key_Pause) Then End
			If KeyHit(Key_Jump) Then Fart=True
		Wend
	EndIf
	
	If Jump#<0 Then Jump#=0
	
	If Not ChannelPlaying(BGM) Then
		BGM=PlayMusic("Overworld.mp3")
		ChannelVolume BGM,.5
	EndIf
	
	If Jump#=0 Then
		For p.pit = Each pit
			If EntityZ(JackLocator)>EntityZ(P\entity)-p\size And EntityZ(JackLocator)<EntityZ(P\Entity)+p\size Then
				Die(1)
				Start()
			EndIf
		Next
	EndIf
	
	Frame#=Frame#+Abs(Speed#/8)
	If Frame#>3 Then Frame# = 0
	
	If KeyDown(Key_Jump) And Jump#<25 And top=False Then
		If Not ChannelPlaying(ChJump) Then
			ChJump=PlaySound(SFXJumpUp)
		EndIf
		Jump#=Jump#+1
		Jumped=True
	Else
		StopChannel ChJump
		If Jump#>=25 Then top=True
		If Jump#>0 Then
			Top=True
			Jump#=Jump#-1
		Else
			If Jumped=True Then
				ChLand=PlaySound(SFXLand)
				Jumped=False
			EndIf
			If top=True Then
				top=False
			EndIf
		EndIf
	EndIf
	
	If DingCount>0 Then
		If Not ChannelPlaying(SFX) Then
			SFX=PlaySound(SFXDing)
			DingCount=DingCount-1
		EndIf
	EndIf
	
	LevelDist=EntityZ(camera)-StartDist
	
	If LevelDist>5000 Then
		CurrentLevel=CurrentLevel+1
		DingCount=10
		LevelDist=0
		StartDist=EntityZ(Camera)
	EndIf
	
	CameraProject(camera,EntityX(JackLocator),EntityY(JackLocator),EntityZ(JackLocator))
	If Health#=2 Then
		DrawImage Jack,ProjectedX#(),ProjectedY#(),Int(Frame+4)
	Else
		DrawImage Jack,ProjectedX#(),ProjectedY#(),Int(Frame)
	EndIf
	Text 0,0,"Extra Lives: "+Int(Lives#)
	Text 0,FontHeight(),"Score: "+Int(Score#+ ( (LevelDist*(CurrentLevel))/100 ) )
	Text 0,FontHeight()*2,"Level: "+CurrentLevel
	WaitTimer(FrameTimer)
	Flip
Wend
End