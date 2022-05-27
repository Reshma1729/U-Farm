package com.example.u_farm.database

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.u_farm.model.Problem
import com.example.u_farm.model.Solution
import com.example.u_farm.model.U_Farm
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.NonCancellable.children
import java.io.File
import java.util.*


class AuthRepository(application: Application){
    private var auth: FirebaseAuth
    private var firebaseDatabase:FirebaseDatabase
    private var reference:DatabaseReference
    public var reference1:DatabaseReference
    public var reference2:DatabaseReference
    private var application:Application
    private var firebaseUserAuthRepository= MutableLiveData<FirebaseUser?>()
    private var userLoggedAuthRepository=MutableLiveData<Boolean?>()
    private var setUserDataRepository=MutableLiveData<Boolean?>()
    private var getUserDataRepository=MutableLiveData<U_Farm?>()
    private var singleRecordDataRepository=MutableLiveData<Boolean?>()

    private var uploadedDataRepository=MutableLiveData<String?>()

    private var setSolutionDataRepository=MutableLiveData<Boolean?>()

    private var setProblemDataRepository=MutableLiveData<Boolean?>()
    private var storage:FirebaseStorage
    private var ProblemDataMutableLiveDataList=MutableLiveData<MutableList<Problem?>>()

    private var SolutionDataMutableLiveDataList=MutableLiveData<MutableList<Solution?>>()

    var problemList=mutableListOf<Problem?>()

    var solutionList=mutableListOf<Solution?>()


    init{
        this.application=application
        firebaseDatabase= FirebaseDatabase.getInstance()
        reference=firebaseDatabase.getReference("UFARMDB")
        reference1=firebaseDatabase.getReference("PROBLEM").push()
        reference2=firebaseDatabase.getReference("SOLUTION").push()
        storage=FirebaseStorage.getInstance()
        auth= FirebaseAuth.getInstance()
        if(auth.currentUser!=null){
            firebaseUserAuthRepository.postValue(auth.currentUser)

        }
    }

    fun getFirebaseUserMutableLiveData(): MutableLiveData<FirebaseUser?>{
        return firebaseUserAuthRepository
    }

    fun getUserLoggedMutableLiveData(): MutableLiveData<Boolean?> {
        return userLoggedAuthRepository
    }

    fun setUserDataMutableLiveData(): MutableLiveData<Boolean?> {
        return setUserDataRepository
    }

    fun setSolutionDataMutableLiveData(): MutableLiveData<Boolean?> {
        return setSolutionDataRepository
    }

    fun setProblemDataMutableLiveData(): MutableLiveData<Boolean?> {
        return setProblemDataRepository
    }



    fun getUserDataMutableLiveData(): MutableLiveData<U_Farm?> {
        return getUserDataRepository
    }


    fun ProblemDataMutableLiveDataList(): MutableLiveData<MutableList<Problem?>> {
        return ProblemDataMutableLiveDataList
    }


    fun SolutionDataMutableLiveDataList(): MutableLiveData<MutableList<Solution?>> {
        return SolutionDataMutableLiveDataList
    }


    fun singleRecordDataMutuableLiveData(): MutableLiveData<Boolean?>{
        return singleRecordDataRepository
    }


    fun uploadedDataMutuableLiveData(): MutableLiveData<String?>{
        return uploadedDataRepository
    }



    fun register(username:String,email:String,password:String){
        if(email.isEmpty()||password.isEmpty()){
            Toast.makeText(application, "Please enter you email address or password", Toast.LENGTH_LONG)
                .show()
            return
        }
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{
                if(!it.isSuccessful) return@addOnCompleteListener
                firebaseUserAuthRepository.postValue(auth.currentUser)
                val ufarm=U_Farm(auth.currentUser!!.uid,username,email,password)
                setUserData(ufarm)
                Log.d("SignUp", "${it.result?.user?.uid}")
            }

            .addOnFailureListener{
                Toast.makeText(application, "${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    fun login(email:String,password: String){
        if(email.isEmpty()||password.isEmpty()){
            Toast.makeText(application, "Please enter you email address or password", Toast.LENGTH_LONG)
                .show()
            return
        }
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{
                if(!it.isSuccessful) return@addOnCompleteListener
                firebaseUserAuthRepository.postValue(auth.currentUser)
                Log.d("SignIn", "${it.result?.user?.uid}")
            }

            .addOnFailureListener{
                Toast.makeText(application,"${it.message}",Toast.LENGTH_LONG).show()
            }
    }

    fun signOut(){
        auth.signOut()
        userLoggedAuthRepository.postValue(true)

    }

    /**U_Farm Model Function**/

    fun setUserData(ufarm: U_Farm){
        reference.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (auth.currentUser?.uid != null) {
                    reference.child(auth.currentUser!!.uid).setValue(ufarm)
                }
                setUserDataRepository.postValue(true)
            }
            override fun onCancelled(error: DatabaseError) {
                setUserDataRepository.postValue(false)
            }
        })
    }

    fun singleRecord(data:String,parameter:String){
        reference.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (auth.currentUser?.uid != null) {
                    reference.child("${auth.currentUser?.uid}/$parameter").setValue(data)
                }
                singleRecordDataRepository.postValue(true)
            }
            override fun onCancelled(error: DatabaseError) {
                singleRecordDataRepository.postValue(false)
            }
        })
    }

    fun getUserData(){
        val userData:Query=firebaseDatabase.getReference("/UFARMDB/${auth.currentUser?.uid}")
        userData.addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val uFarm = snapshot.getValue(U_Farm::class.java)
                if (uFarm != null) {
                    getUserDataRepository.postValue(uFarm)
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    /**Problem Model Function**/

    fun ProblemDataList(){
        val ref=firebaseDatabase.getReference("PROBLEM")
    ref.addValueEventListener(object :ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
                        for (postSnapshot in snapshot.children) {
                            val problem = postSnapshot.getValue(Problem::class.java)
                            if (problem != null) {
                                problemList.add(problem)
                            }
                        }
                        ProblemDataMutableLiveDataList.postValue(problemList)
                    }
                        override fun onCancelled(error: DatabaseError) {

                }

            })

        }



    /**Solution Model Function**/

    fun SolutionDataList(problemUid:String){
        val ref=firebaseDatabase.getReference("SOLUTION")
        ref.addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    val solution = postSnapshot.getValue(Solution::class.java)
                    if (solution?.problemUid != problemUid) {
                        solutionList.add(solution)
                    }
                }
                SolutionDataMutableLiveDataList.postValue(solutionList)
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    fun singleRecordProblem(data:String,parameter:String){
        reference1.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (auth.currentUser?.uid != null) {
                    reference1.child("/$parameter/").setValue(data)
                }
                singleRecordDataRepository.postValue(true)
            }
            override fun onCancelled(error: DatabaseError) {
                singleRecordDataRepository.postValue(false)
            }
        })
    }

    fun setProblemData(problem: Problem){
        reference1.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (auth.currentUser?.uid != null) {
                    reference1.setValue(problem)
                }
                setProblemDataRepository.postValue(true)
            }
            override fun onCancelled(error: DatabaseError) {
                setProblemDataRepository.postValue(false)
            }
        })
    }



    fun setSolutionData(solution: Solution){

        reference2.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (auth.currentUser?.uid != null) {
                    reference2.setValue(solution)
                }
                setSolutionDataRepository.postValue(true)
            }
            override fun onCancelled(error: DatabaseError) {
                setSolutionDataRepository.postValue(false)
            }
        })
    }


    fun uploadImageToFirebaseStorage(image: Uri,folder:String) {
        val ref =FirebaseStorage.getInstance().getReference("/$folder/" + UUID.randomUUID().toString())

        val uploadTask = ref.putFile(image)
        val urlTasK = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl

            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    Log.d("AuthRepository", "Downloaded URL: is ${downloadUri}")
                    val downloadUrl = downloadUri.toString()
                    uploadedDataRepository.postValue(downloadUrl)
                } else {
                    uploadedDataRepository.postValue(null)
                }
            }.addOnFailureListener {
                uploadedDataRepository.postValue(null)
            }
    }


    fun uploadAudioToFirebaseStorage(filename: File) {
        val ref =FirebaseStorage.getInstance().getReference("/Audio/" + UUID.randomUUID().toString())
        val uri:Uri=Uri.fromFile(filename)
        val uploadTask = ref.putFile(uri)
        val urlTasK = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl

        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                Log.d("AuthRepository", "Downloaded URL: is ${downloadUri}")
                val downloadUrl = downloadUri.toString()
                uploadedDataRepository.postValue(downloadUrl)
            } else {
                uploadedDataRepository.postValue(null)
            }
        }.addOnFailureListener {
            uploadedDataRepository.postValue(null)
        }
    }
}
