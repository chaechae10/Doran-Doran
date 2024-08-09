package com.dorandoran.backend.Member.domain;

import com.dorandoran.backend.Member.dto.req.LoginRequest;
import com.dorandoran.backend.Member.dto.req.MemberUpdateRequestDTO;
import com.dorandoran.backend.Member.dto.req.SendResetPasswordReq;
import com.dorandoran.backend.Member.dto.req.SignUpRequest;
import com.dorandoran.backend.Member.dto.res.MemberResponseDTO;
import com.dorandoran.backend.Member.dto.res.SendResetPasswordRes;
import com.dorandoran.backend.Member.exception.MemberNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final Mailservice mailservice;
    private final PasswordEncoder passwordEncoder;
    private final HttpSession session;

    public String determineHomePage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long memberId = (Long) session.getAttribute("memberId");

        Member loginMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with ID: " + memberId));

        return "Main Page, Hello " + loginMember.getName();
    }

    public void signUp(SignUpRequest signUpRequest) {
        Member member = Member.builder()
                .name(signUpRequest.getName())
                .loginId(signUpRequest.getLoginId())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .email(signUpRequest.getEmail())
                .address(signUpRequest.getAddress())
                .build();

        memberRepository.save(member);
    }

    public void login(LoginRequest loginRequest) {
        Member member = memberRepository.findByLoginId(loginRequest.getLoginId())
                .orElseThrow(() -> new MemberNotFoundException("Invalid login ID or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new MemberNotFoundException("Invalid login ID or password");
        }

        // 로그인 성공 시 세션에 사용자 정보를 저장
        session.setAttribute("memberId",member.getId());
    }

    public String findLoginIdByEmail(String token) {
        Member member = memberRepository.findByResetToken(token)
                .orElseThrow(() -> new MemberNotFoundException("Invalid or expired token"));
        return "Login ID: " + member.getLoginId();
    }

    public SendResetPasswordRes sendResetPassword(SendResetPasswordReq resetPasswordEmailReq) {
        Member member = memberRepository.findByLoginIdAndEmail(
                        resetPasswordEmailReq.getLoginId(), resetPasswordEmailReq.getEmail())
                .orElseThrow(() -> new MemberNotFoundException("No member found with this login ID and email"));

        String uuid = mailservice.sendResetPasswordEmail(member.getEmail());
        member.setResetToken(uuid);
        memberRepository.save(member);
        String resetPasswordLink = "https://yourdomain.com/reset-password/" + uuid;

        return SendResetPasswordRes.builder()
                .resetPasswordLink(resetPasswordLink)
                .build();
    }

    public void resetPassword(String uuid, String newPassword) {
        Member member = memberRepository.findByResetToken(uuid)
                .orElseThrow(() -> new MemberNotFoundException("Invalid or expired token"));

        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    public MemberResponseDTO getMemberInfo(){
        // 현재 인증된 사용자의 정보 가져옴.
        CustomUserDetails memberDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginId = memberDetails.getUsername();

        // 로그인 ID를 사용하여 회원 정보를 조회.
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // DTO로 변환하여 반환.
        return new MemberResponseDTO(member.getId(), member.getName(), member.getEmail(), member.getAddress());
    }

    public MemberResponseDTO updateMemberInfo(MemberUpdateRequestDTO memberUpdateRequestDTO) {
        CustomUserDetails memberDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member member = memberDetails.getMember();

        // 닉네임 중복 확인
        if (!member.getName().equals(memberUpdateRequestDTO.getName()) && memberRepository.existsByName(memberUpdateRequestDTO.getName())) {
            throw new IllegalArgumentException("닉네임이 이미 사용 중입니다.");
        }

        // 비밀번호 검증 및 변경
        if (memberUpdateRequestDTO.getPassword() != null && !memberUpdateRequestDTO.getPassword().isEmpty()) {
            if (!memberUpdateRequestDTO.getPassword().equals(memberUpdateRequestDTO.getPasswordCheck())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }

            if (!isValidPassword(memberUpdateRequestDTO.getPassword())) {
                throw new IllegalArgumentException("비밀번호 형식이 잘못되었습니다.");
            }

            member.setPassword(passwordEncoder.encode(memberUpdateRequestDTO.getPassword()));
        }

        // 회원 정보 업데이트
        member.update(
                memberUpdateRequestDTO.getName(),
                memberUpdateRequestDTO.getAddress(),
                memberUpdateRequestDTO.getEmail(),
                member.getPassword()
        );
        memberRepository.save(member);
        return new MemberResponseDTO(member.getId(), member.getName(), member.getEmail(), member.getAddress());
    }

    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$");
    }

    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }
}
