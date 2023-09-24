; Updates the pits of death
Function UpdatePits()
	For P.Pit = Each Pit
		If EntityZ(Camera)>EntityZ(P\entity)+P\Size Then
			TranslateEntity P\entity,0,0,20000
			Score#=Score#+25
		EndIf
	Next
End Function

; Updates the stars (score increasers)
Function UpdateStars()
	For St.Star = Each Star
		For p.pit = Each pit
			If EntityZ(St\Entity)>EntityZ(P\entity)-p\size And EntityZ(St\Entity)<EntityZ(P\Entity)+p\size Then
				TranslateEntity St\entity,0,0,3000
			EndIf
		Next
		If Sqr( ((EntityX(St\Entity)-EntityX(Camera))^2) + ((EntityZ(St\Entity)-EntityZ(Camera)-30)^2) )<10 And Jump<3 Then
			PlaySound(SFXStar)
			Score#=Score#+100
			TranslateEntity St\Entity,0,0,3000
		EndIf
		If EntityZ(Camera)>EntityZ(St\entity) Then TranslateEntity St\entity,0,0,3000
	Next
End Function

; Updates the pipes and handles their collisions
Function UpdatePipes()
	For Pp.Pipe = Each Pipe
		If Sqr( ((EntityX(Pp\Entity)-EntityX(JackLocator))^2) + ((EntityZ(Pp\Entity)-EntityZ(JackLocator))^2) )<10 And EntityY(JackLocator)-3<Pp\Height Then
			ReverseTimer#=20
			PlaySound(SFXCollide)
			If Pp\Contents>0 Then
				DropPowerup(Pp\Contents,EntityX(Pp\Entity),Pp\Height,EntityZ(Pp\Entity))
				Pp\Contents=0
			EndIf
		EndIf
		If EntityZ(Camera)>EntityZ(pp\entity) Then TranslateEntity pp\entity,0,0,10000
	Next
End Function

; Updates the pillars of fire and handles their collisions
Function UpdatePillars()
	For Pl.Pillar = Each Pillar
		If Sqr( ((EntityX(Pl\Entity)-EntityX(Camera))^2) + ((EntityZ(Pl\Entity)-EntityZ(Camera)-30)^2) )<10 And EntityY(JackLocator)<12 Then
			PlaySound(SFXCollide)
			HurtJack()
		EndIf
		If EntityZ(Pl\Entity)>10000 And EntityZ(Pl\Entity)<18000 Then TranslateEntity Pl\entity,0,0,3000
		If EntityZ(Camera)>EntityZ(Pl\entity) Then TranslateEntity Pl\entity,0,0,3000
	Next
End Function

; This handles setup of a new life
Function Start()
	
	.relocate
	For p.pit = Each pit
		If EntityZ(JackLocator)>EntityZ(P\entity)-p\size And EntityZ(JackLocator)<EntityZ(P\Entity)+p\size Then
			PositionEntity Camera,EntityX(Camera),EntityY(Camera),EntityZ(P\Entity)+p\size
			PositionEntity JackLocator,EntityX(camera),Jump#+EntityY(Camera)-12.5,EntityZ(Camera)+30
			Goto relocate
		EndIf
	Next
	For Pl.pillar = Each pillar
		If EntityDistance(JackLocator,Pl\Entity)<20 Then
			PositionEntity Camera,EntityX(Camera),EntityY(Camera),EntityZ(Pl\Entity)+15
			PositionEntity JackLocator,EntityX(camera),Jump#+EntityY(Camera)-12.5,EntityZ(Camera)+30
			Goto relocate
		EndIf
	Next
	
	While DingCount>0
		Cls
		TileImage GFX_Background,EntityX(Camera)/1000,0
		UpdateWorld
		RenderWorld
		If Not ChannelPlaying(SFX) Then
			SFX=PlaySound(SFXDing)
			DingCount=DingCount-1
		EndIf
		WaitTimer(FrameTimer)
		Flip
	Wend

	Health#=1
	Local Pos#=-GraphicsHeight()*3.5
	
	EntityAlpha shadow,0
	
	HasGun=False
	
	BGM=PlayMusic("Start.mp3")
	ChannelVolume BGM,.5

	While Not Done
		Cls
		Pos=Pos+(GraphicsHeight()/45)
		
		TileImage GFX_Background,EntityX(Camera)/1000,0
		UpdateWorld()
		RenderWorld()
		
		CameraProject(camera,0,0,EntityZ(Camera)+30)
		DrawImage Jack,GraphicsWidth()/2,ProjectedY#()-60-Pos,1
		
		If Not ChannelPlaying(BGM) Then Done=True
		If KeyHit(1) Then End
		Text 0,0,"Extra Lives: "+Int(Lives#)
		Text 0,FontHeight(),"Score: "+Int(Score#+ ( (LevelDist*(CurrentLevel))/100 ) )
		Text 0,FontHeight()*2,"Level: "+CurrentLevel
		WaitTimer(FrameTimer)
		Flip
	Wend
	
	EntityAlpha Shadow,.5
	Jump=Pos
	Jumped=True
	top=True
End Function

; This handles the death of a player
Function Die(Method)
	Lives#=Lives#-1
	StopChannel(BGM)
	BGM=PlaySound(SFXLifeLost)
	
	EntityAlpha Shadow,0
	
	Local pos#=0
		
	While Not Done
		Cls
		
		updateparticles()
		
		TileImage GFX_Background,EntityX(Camera)/1000,0
		UpdateWorld
		RenderWorld
		
		Pos=Pos-(GraphicsHeight()/45)
		
		If Not ChannelPlaying(BGM) Then
			Done=True
		EndIf

		CameraProject(camera,0,0,EntityZ(Camera)+30)
		
		If method=1 Then
			Color 0,0,0
			Rect 0,ProjectedY#(),GraphicsWidth(),GraphicsHeight()-ProjectedY#()
			Color 255,255,255
			If Health=2 Then
				DrawImage Jack,GraphicsWidth()/2,ProjectedY#()-60-Pos,4
			Else
				DrawImage Jack,GraphicsWidth()/2,ProjectedY#()-60-Pos,1
			EndIf
		Else
			DrawImage Pain,GraphicsWidth()/2,ProjectedY#()-60-Pos
		EndIf

		If KeyHit(1) Then End
		
		Text 0,0,"Extra Lives: "+Int(Lives#)
		Text 0,FontHeight(),"Score: "+Int(Score#+ ( (LevelDist*(CurrentLevel))/100 ) )
		Text 0,FontHeight()*2,"Level: "+CurrentLevel
		WaitTimer(FrameTimer)
		
		Flip
	Wend	
	If Lives#<0 Then
		SFX=PlaySound(SFXGameOver)
		While ChannelPlaying(SFX)
		Cls
		Text GraphicsWidth()/2,GraphicsHeight()/2,"GAME OVER",1,1
		Text GraphicsWidth()/2,(GraphicsHeight()/2)+FontHeight(),"Score: "+Int(Score#+ ( (LevelDist*(CurrentLevel))/100 ) ),1,1
		Flip
		Wend
		End
	EndIf
	pos=0
	jump=0
End Function
	
; Updates the enemies
Function UpdateMeanies()
	For M.Meanie = Each Meanie
		If EntityZ(M\Entity)<EntityZ(Camera) Then
			TranslateEntity M\Entity,0,0,4000
		EndIf
		
		Select M\Movement
			Case 1:
				TranslateEntity M\Entity,-2,0,0
			Case 0:
				TranslateEntity M\Entity,2,0,0
		End Select
		
		For p.pit = Each pit
			If EntityZ(M\Entity)>EntityZ(P\entity)-p\size And EntityZ(M\Entity)<EntityZ(P\Entity)+p\size And m\tex<>1 Then
				M\tex = 1
				PaintEntity M\Entity,OldMeanieTex
			Else If M\Tex<>0 And EntityZ(M\Entity)<EntityZ(P\entity)-p\size And EntityZ(M\Entity)>EntityZ(P\Entity)+p\size Then
				M\tex=0
				PaintEntity M\Entity,MeanieTex
			EndIf
		Next
		
		If m\jitter=1 Or M\Tex=1 Then
			M\Shake=M\Shake+10
		Else
			M\Shake=M\Shake-10
		EndIf
		
		If M\Shake=>40 And m\jitter=1 Then
			m\Jitter=0
		Else If M\Shake=<-40 And m\jitter=0 Then
			m\jitter=1
		EndIf
				
		RotateEntity M\Entity,0,M\Shake,0
		
		M\Timer=M\Timer+1
		If M\Timer>120 Then
			If M\Movement=1 Then
				M\Movement=0
			Else
				M\Movement=1
			EndIf
			M\Timer=0
		EndIf
		If EntityDistance(M\Entity,shadow)<10 And EntityY(JackLocator)<3 Then
			createparticle(EntityX(M\Entity),EntityY(M\Entity),EntityZ(M\Entity))
			PlaySound(SFXExplode)
			TranslateEntity M\Entity,0,0,4000
			HurtJack()
		Else If EntityDistance(M\Entity,shadow)<10 And EntityY(JackLocator)=3 Then
			createparticle(EntityX(M\Entity),EntityY(M\Entity),EntityZ(M\Entity))
			PlaySound(SFXExplode)
			TranslateEntity M\Entity,0,0,4000
			Top=False
		Else If EntityCollided(M\Entity,2) Then
			createparticle(EntityX(M\Entity),EntityY(M\Entity),EntityZ(M\Entity))
			PlaySound(SFXExplode)
			Score#=Score#+100
			EntityType M\Entity,0
			TranslateEntity M\Entity,0,0,4000
			EntityType M\Entity,1
		EndIf
	Next
End Function

; Creates the particles that fly from killed enemies
Function createparticle(x#,y#,z#)
	For a = 1 To 5
		f.frags = New frags
		f\entity = CopyEntity(spark)
		PositionEntity f\entity, x#, y#, z#
		f\speed# = Rnd(3,4)
		f\alpha# = 1
		RotateEntity f\entity, Rand(360), Rand(360), Rand(360)
;		EntityColor f\entity, Rand(255), Rand(255), Rand(255)
		EntityAlpha f\entity, f\alpha#
		ScaleSprite f\entity, .5, .5
	Next
End Function

; Updates the particles that fly from killed enemies
Function updateparticles()
	For f.frags = Each frags
		If f\alpha# > 0
			MoveEntity f\entity, 0, 0, f\speed#
			f\alpha# = f\alpha# - 0.1
		Else
			FreeEntity f\entity
			Delete f
		EndIf
	Next
End Function

; Fires a new bullet from Jack's 3D locator
Function FireBullet()
	Bul.Bullet = New Bullet
	Bul\Entity = LoadSprite("Bullet.png",7)
	ScaleSprite Bul\Entity,2,2
	PositionEntity Bul\Entity,EntityX(JackLocator),EntityY(JackLocator),EntityZ(JackLocator)
	EntityType Bul\Entity,2
	Bul\Life=180
End Function

; Updates the bullets in the world and removes them once they decay
Function UpdateBullets()
	For Bul.Bullet = Each Bullet
		Bul\Life = Bul\Life - 1
		If Bul\Life =<0 Then
			HideEntity Bul\Entity
			Delete Bul
		Else
			MoveEntity Bul\Entity,0,0,20
			If EntityCollided(Bul\Entity,1) Then
				HideEntity Bul\Entity
				Delete Bul
			EndIf
		EndIf
	Next
End Function

; Drops a powerup from a pipe using the pipe's location
Function DropPowerup(Form,X#,Y#,Z#)
	D.Powerup = New Powerup
		Select Form:
			Case 1:
				D\Entity = LoadSprite("Gun.png",7)
			Case 2:
				D\Entity = LoadSprite("Potion.png",7)
			Case 3:
				D\Entity = LoadSprite("Mush.png",7)
		End Select
		ScaleSprite D\Entity,3,3
		PositionEntity D\Entity,X#,Y#*2,Z#-10
		D\Form = Form
End Function

; Updates the powerups in the world, gives them to Jack if he's near enough to one and removes them once they hit the ground
Function UpdatePowerups()
	For D.Powerup = Each Powerup
		TranslateEntity D\Entity,0,-.5,0
		If EntityY(D\Entity)<0 Then
			HideEntity D\Entity
			Delete D
		Else
			If EntityDistance(D\Entity,JackLocator)<8 Then
				Select D\Form
					Case 1:
						HasGun = True
						PlaySound(SFXPickup)
						Score#=Score#+100
					Case 2:
						Health# = 2
						PlaySound(SFXPickup)
						Score#=Score#+100
					Case 3:
						HurtJack()
				End Select
				HideEntity D\Entity
				Delete D
			EndIf
		EndIf
	Next
End Function

; Apply pain to Jack (called from collison with painful object)
Function HurtJack()
	If Health#=2 Then
		Health#=1
		HasGun=False
	Else If Health#=1 Then
		Die(0)
		Start()
	EndIf
End Function