Function SetupResolution()
	.retry
	
	SetBuffer(BackBuffer())
	
	If FileType(GetEnv("APPDATA")+"/WorldRunner/res.dat")=1 Then
		If changedres=False Then
			Cls
			Text GraphicsWidth()/2,GraphicsHeight()/2,"Change Resolution? (Y/N)",1,1
			Flip
			key=WaitKey()
			If key=121 Then
				DeleteFile GetEnv("APPDATA")+"/WorldRunner/res.dat"
				Goto retry
			EndIf
		EndIf
		
		Filestream=ReadFile(GetEnv("APPDATA")+"/WorldRunner/res.dat")
		GraphicX#=ReadLine(Filestream)
		GraphicY#=ReadLine(Filestream)
		GraphicDepth#=ReadLine(Filestream)
		GraphicMode#=ReadLine(Filestream)
		CloseFile Filestream
		
		If GfxModeExists(GraphicX#,GraphicY#,GraphicDepth#) Then
			Graphics3D GraphicX#,GraphicY#,GraphicDepth#,GraphicMode#
		Else
			Cls
			Print "Failed to load graphics!"
			Delay 500
			DeleteFile GetEnv("APPDATA")+"/WorldRunner/res.dat"
			Goto retry
		EndIf
	Else
		ChangeGFXMode()
		If FileType(GetEnv("APPDATA")+"/WorldRunner")<>2 Then
			CreateDir GetEnv("APPDATA")+"/WorldRunner"
		EndIf
		Filestream=WriteFile(GetEnv("APPDATA")+"/WorldRunner/res.dat")
		WriteLine Filestream, Int(GraphicX#)
		WriteLine Filestream, Int(GraphicY#)
		WriteLine Filestream, Int(GraphicDepth#)
		WriteLine Filestream, Int(GraphicMode#)
		CloseFile Filestream
		changedres=True
		Goto retry
	EndIf
	HidePointer()
End Function

Function SetupControls()
	If FileType(GetEnv("APPDATA")+"/WorldRunner/controls.dat")=1 Then
		Filestream=ReadFile(GetEnv("APPDATA")+"/WorldRunner/controls.dat")
			Key_Jump = ReadLine(Filestream)
			Key_Shoot = ReadLine(Filestream)
			Key_SpeedUp = ReadLine(Filestream)
			Key_Left = ReadLine(Filestream)
			Key_Right = ReadLine(Filestream)
			Key_Pause = ReadLine(Filestream)
		CloseFile Filestream
	EndIf
		Cls
		Text GraphicsWidth()/2,GraphicsHeight()/2,"Remap controls? (Y/N)",1,1
		Flip
		key=WaitKey()
		If key=121 Then
			Delay 200
			Key_Jump=ChangeKey("Jump")
			Delay 200
			Key_Shoot=ChangeKey("Shoot")
			Delay 200
			Key_SpeedUp=ChangeKey("SpeedUp")
			Delay 200
			Key_Left=ChangeKey("Left")
			Delay 200
			Key_Right=ChangeKey("Right")
			Delay 200
			Key_Pause=ChangeKey("Pause")
			
			Filestream=WriteFile(GetEnv("APPDATA")+"/WorldRunner/controls.dat")
				WriteLine Filestream, Key_Jump
				WriteLine Filestream, Key_Shoot
				WriteLine Filestream, Key_SpeedUp
				WriteLine Filestream, Key_Left
				WriteLine Filestream, Key_Right
				WriteLine Filestream, Key_Pause
			CloseFile Filestream
		EndIf
End Function

Function ChangeGFXMode()
		Modes#=CountGfxModes3D()
		Mode#=1
		FullScreen=1
		Local Fart=False
		
		Graphics 330,100,16,2
		
		While Not Fart
			Cls
			Text 1,1*FontHeight(),"Click on an option to toggle it foreward."
			Text 1,2*FontHeight(),"Right click to toggle it backward."
			Text 1,3*FontHeight(),"Select your graphics settings:"
			Text 1,4*FontHeight(),GfxModeWidth(Mode)+"x"+GfxModeHeight(Mode)+"["+GfxModeDepth(Mode)+"]"
			If Fullscreen Then
				Text 1,5*FontHeight(),"Fullscreen"
			Else
				Text 1,5*FontHeight(),"Windowed"
			EndIf
			Text GraphicsWidth()-StringWidth("[Ok]"),GraphicsHeight()-FontHeight(),"[Ok]"
			
			If KeyHit(1) Then
				End
			EndIf
			
			If Windowed3D()=False Then
				Fullscreen=False
			EndIf
			
			If MouseHit(1) Then
				If MouseY()>4*FontHeight() And MouseY()<5*FontHeight() Then
					.fagain
					Mode#=Mode#+1
					If Mode#>Modes# Then
						Mode#=1
					EndIf
					If GfxMode3D(mode) = False Then Goto fagain
				EndIf
				If MouseY()>5*FontHeight() And MouseY()<6*FontHeight() Then
					If Fullscreen Then
						Fullscreen = False
					Else
						fullscreen = True
					EndIf
				EndIf
				If MouseY()>GraphicsHeight()-FontHeight() And MouseY()<GraphicsHeight() Then
					Fart=True
				EndIf
			EndIf
			
			If MouseHit(2) Then
				If MouseY()>4*FontHeight() And MouseY()<5*FontHeight() Then
					.bagain
					Mode#=Mode#-1
					If Mode#<1 Then
						Mode#=Modes#
					EndIf
					If GfxMode3D(mode) = False Then Goto bagain
				EndIf
				If MouseY()>5*FontHeight() And MouseY()<6*FontHeight() Then
					If Fullscreen Then
						Fullscreen = False
					Else
						fullscreen = True
					EndIf
				EndIf
				If MouseY()>GraphicsHeight()-FontHeight() And MouseY()<GraphicsHeight() Then
					Fart=True
				EndIf
			EndIf
			
			Mouse()
			Flip
		Wend
		GraphicX#=GfxModeWidth(mode)
		GraphicY#=GfxModeHeight(mode)
		GraphicDepth#=GfxModeDepth(mode)	
		If Fullscreen Then
			Fmode=1
		Else
			Fmode=2
		EndIf
		GraphicMode#=Fmode
End Function

Function Mouse()
	Color 255,255,255
	MX#=MouseX()
	MY#=MouseY()
	Line MX-5,MY,MX+5,MY
	Line MX,MY-5,MX,MY+5
End Function


Function FindScancode(scn)
	For ac.ASCII_Convert = Each ASCII_Convert
		If ac\scn=scn Then
			Return ac\ascii
		EndIf
	Next
End Function

Function ChangeKey(Control$)
	FlushKeys()
	While Not fart
		Cls
		Text GraphicsWidth()/2,GraphicsHeight()/2,"Press the key you want to use for "+Control$,1,1
		Key_Return=GetSkey()
		If Key_Return<>0 Then
			Fart=True
		EndIf
		Flip
	Wend
	Return Key_Return
End Function

Function GetSKey()
	For Skey = 1 To 255
	If KeyDown(Skey) Then Return Skey
	Next
End Function

Function ScanCode_To_Ascii$(Key#)
	If key =  42 Then Return "Left Shift"
	If key = 54 Then Return "Right Shift"
	If key = 57 Then Return "Space"
	If key = 28 Then Return "Enter"
	If key = 219 Then Return "Left Meta"
	If key = 220 Then Return "Right Meta"
	If key = 29 Then Return "Left Ctrl"
	If key = 157 Then Return "Right Ctrl"
	If key = 56 Then Return "Left Alt"
	If key = 184 Then Return "Right Alt"
	If key = 211 Then Return "Delete"
	If key = 207 Then Return "End"
	If key = 209 Then Return "Page Down"
	If key = 201 Then Return "Page Up"
	If key = 199 Then Return "Home"
	If key = 210 Then Return "Insert"
	If key = 14 Then Return "Backspace"
	If key = 15 Then Return "Tab"
	If key = 221 Then Return "Context Menu Key"
	If key >= 59 And key <=68 Then
		F#=Key-59
		Return "F"+Int(F#)
	EndIf
	If key = 87 Then Return "F11"
	If key = 88 Then Return "F12"

	A#=FindScancode(key#)
	Return Chr(A)
End Function

.ScanCodesToAscii_Data
Data 1,27,1
Data 1,96,41
Data 1,49,2
Data 1,50,3
Data 1,51,4
Data 1,52,5
Data 1,53,6
Data 1,54,7
Data 1,55,8
Data 1,56,9
Data 1,57,10
Data 1,48,11
Data 1,45,12
Data 1,61,13
Data 1,8,14
Data 1,3,210
Data 1,1,199
Data 1,5,201
Data 1,6,209
Data 1,2,207
Data 1,4,211
Data 1,9,15
Data 1,113,16
Data 1,119,17
Data 1,101,18
Data 1,114,19
Data 1,116,20
Data 1,121,21
Data 1,117,22
Data 1,105,23
Data 1,111,24
Data 1,112,25
Data 1,91,26
Data 1,93,27
Data 1,92,43
Data 1,97,30
Data 1,115,31
Data 1,100,32
Data 1,102,33
Data 1,103,34
Data 1,104,35
Data 1,106,36
Data 1,107,37
Data 1,108,38
Data 1,59,39
Data 1,39,40
Data 1,13,28
Data 1,122,44
Data 1,120,45
Data 1,99,46
Data 1,118,47
Data 1,98,48
Data 1,110,49
Data 1,109,50
Data 1,44,51
Data 1,46,52
Data 1,47,53
Data 1,32,57
Data 1,31,203
Data 1,29,208
Data 1,30,205
Data 1,28,200
Data 2,126,41
Data 2,33,2
Data 2,64,3
Data 2,35,4
Data 2,36,5
Data 2,37,6
Data 2,94,7
Data 2,38,8
Data 2,42,9
Data 2,40,10
Data 2,41,11
Data 2,95,12
Data 2,43,13
Data 2,81,16
Data 2,87,17
Data 2,69,18
Data 2,82,19
Data 2,84,20
Data 2,89,21
Data 2,85,22
Data 2,73,23
Data 2,79,24
Data 2,80,25
Data 2,123,26
Data 2,125,27
Data 2,124,43
Data 2,9,15
Data 2,65,30
Data 2,83,31
Data 2,68,32
Data 2,70,33
Data 2,71,34
Data 2,72,35
Data 2,74,36
Data 2,75,37
Data 2,76,38
Data 2,58,39
Data 2,34,40
Data 2,90,44
Data 2,88,45
Data 2,67,46
Data 2,86,47
Data 2,66,48
Data 2,78,49
Data 2,77,50
Data 2,60,51
Data 2,62,52
Data 2,63,53
Data 999