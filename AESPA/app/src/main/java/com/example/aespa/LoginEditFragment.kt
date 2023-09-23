    package com.example.aespa

    import android.app.Activity
    import android.app.Activity.RESULT_OK
    import android.app.Dialog
    import android.content.ContentValues.TAG
    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.activityViewModels
    import androidx.lifecycle.ViewModelProvider
    import com.example.aespa.databinding.LoginEditFragmentBinding
    import com.kakao.sdk.auth.model.OAuthToken
    import com.kakao.sdk.common.model.ClientError
    import com.kakao.sdk.common.model.ClientErrorCause
    import com.kakao.sdk.user.UserApiClient
    import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
    import io.reactivex.rxjava3.core.Completable
    import io.reactivex.rxjava3.schedulers.Schedulers
    import java.io.Serializable

    class LoginEditFragment : Fragment() {
        val viewModel: ButtonViewModel by activityViewModels()


        private fun showProgressDialog(): Dialog {
            val progressDialog = Dialog(requireContext())
            progressDialog.setContentView(R.layout.progressbar)
            progressDialog.setCancelable(false)
            progressDialog.show()
            return progressDialog
        }
        private var inform: UserInfo? = null

        data class UserInfo(
            val id: Long,
            val nickname: String,
            // 추가 필드 및 사용자 정보 필드 추가 가능
        ) : Serializable
        // 사용자 정보를 저장할 변수


    // ...

        // ...
        private fun performLogin(): Completable {
            return Completable.create { emitter ->
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireActivity())) {
                    UserApiClient.instance.loginWithKakaoTalk(requireActivity()) { token, error ->
                        // ...
                        UserApiClient.instance.me { user, userError ->
                            if (userError != null) {
                                emitter.onError(Throwable("사용자 정보 가져오기 실패"))
                            } else {
                                // 사용자 정보를 User에서 UserInfo로 변환하여 저장
                                inform = UserInfo(
                                    id = user?.id ?: 0,
                                    nickname = user?.kakaoAccount?.profile?.nickname ?: ""

                                )
                                Log.d("이야", "User: ${user?.kakaoAccount?.profile?.nickname}")
                                Log.d("가저온정보1","${inform!!.id}   ${inform!!.nickname}")
                                // 결과 인텐트 설정
                                val resultIntent = Intent()
                                val bundle = Bundle()
                                bundle.putSerializable("userInfo", inform)
                                resultIntent.putExtra("Nickname", inform!!.nickname)
                                resultIntent.putExtra("userId", inform!!.id)
                                resultIntent.putExtra("userBundle", bundle)
                                resultIntent.putExtra("loginState", true)
                                activity?.setResult(Activity.RESULT_OK, resultIntent)
                                requireActivity().finish()
                                emitter.onComplete()
                            }
                        }
                    }
                } else {
                    UserApiClient.instance.loginWithKakaoAccount(requireActivity(), callback = { token, error ->
                        // ...
                        UserApiClient.instance.me { user, userError ->
                            if (userError != null) {
                                emitter.onError(Throwable("사용자 정보 가져오기 실패"))
                            } else {
                                // 사용자 정보를 User에서 UserInfo로 변환하여 저장
                                inform = UserInfo(
                                    id = user?.id ?: 0,
                                    nickname = user?.kakaoAccount?.profile?.nickname ?: ""
                                )
                                // 결과 인텐트 설정
                                val resultIntent = Intent()
                                val bundle = Bundle()
                                bundle.putSerializable("userInfo", inform)
                                resultIntent.putExtra("Nickname", inform!!.nickname)
                                resultIntent.putExtra("userId", inform!!.id)
                                resultIntent.putExtra("userBundle", bundle)
                                resultIntent.putExtra("loginState", true)
                                activity?.setResult(Activity.RESULT_OK, resultIntent)
                                requireActivity().finish()
                                emitter.onComplete()
                            }
                        }
                    })
                }
            }
        }

        private fun performLogout(): Completable {
            return Completable.create { emitter ->
                UserApiClient.instance.logout { error ->
                    if (error != null) {
                        emitter.onError(Throwable("로그아웃 실패"))
                    } else {
                        Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨")
                        viewModel.loginable.value = false
                        Toast.makeText(requireContext(),"로그아웃 성공!",Toast.LENGTH_SHORT)
                        Log.d("로그아웃","로그아웃")
                        val resultIntent = Intent()
                        resultIntent.putExtra("loginState", false) // 전달할 데이터 설정
                        activity?.setResult(Activity.RESULT_OK, resultIntent)
                        requireActivity().finish()
                        emitter.onComplete()
                    }
                }
            }
        }





        private lateinit var binding: LoginEditFragmentBinding // 바인딩 객체 선언
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Toast.makeText(requireContext(),"로그인 성공!",Toast.LENGTH_SHORT)
                Log.i(TAG, "카카오계정으로 로그인에 성공하셨습니다. 토큰이 존재 ${token.accessToken}")
                val resultIntent = Intent()
                resultIntent.putExtra("loginState", true) // 전달할 데이터 설정
                activity?.setResult(Activity.RESULT_OK, resultIntent)
                requireActivity().finish()
            }
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            binding = LoginEditFragmentBinding.inflate(inflater, container, false) // 뷰 바인딩 초기화
            binding.imageView6.setOnClickListener {
                val progressDialog = showProgressDialog()

                performLogin()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally { progressDialog.dismiss() }
                    .subscribe({
                        // 로그인 성공 시 처리
                        Toast.makeText(requireContext(), "로그인 성공!", Toast.LENGTH_SHORT).show()
                        // ... (이전 코드와 동일한 성공 처리)
                    }, { error ->
                        // 로그인 실패 시 처리
                        Toast.makeText(requireContext(), "로그인 실패: ${error.message}", Toast.LENGTH_SHORT)
                            .show()
                    })
            }
            binding.imageView7.setOnClickListener {
                val progressDialog = showProgressDialog()


                performLogout()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally { progressDialog.dismiss() }
                    .subscribe({
                        // 로그아웃 성공 시 처리
                        Toast.makeText(requireContext(), "로그아웃 성공!", Toast.LENGTH_SHORT).show()
                        // ... (이전 코드와 동일한 성공 처리)
                    }, { error ->
                        // 로그아웃 실패 시 처리
                        Toast.makeText(
                            requireContext(),
                            "로그아웃 실패: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    })


            }
            binding.imageView8.setOnClickListener{

            }

            return binding.root // 뷰 계층 구조 반환
        }
    }
