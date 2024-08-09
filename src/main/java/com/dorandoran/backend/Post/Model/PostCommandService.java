package com.dorandoran.backend.Post.Model;

import com.dorandoran.backend.File.Model.FileRepository;
import com.dorandoran.backend.File.Model.S3ImageService;
import com.dorandoran.backend.Member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final S3ImageService s3ImageService;
    private final FileRepository fileRepository;

    /**
     * 게시물 저장
     */
//    @Transactional
//    public Long savePost(PostRequestDTO postRequestDTO) {
//        Member findMember = memberRepository.findById(postRequestDTO.getMember_id())
//                .orElseThrow(() -> new MemberNotFoundException("멤버가 존재하지 않습니다."));
//
//        //파일 업로드
////        String imageUrl = s3ImageService.upload(postRequestDTO.getFile());
//
//        //파일 엔티티 생성
//        File fileEntity = new File(postRequestDTO.getFile().getOriginalFilename());
//        fileEntity.setAccessUrl(imageUrl); // S3 URL 설정
//        fileEntity.setFileSize(postRequestDTO.getFile().getSize());
//        fileEntity.setFileType(postRequestDTO.getFile().getContentType());
//        fileEntity.setFilePath(imageUrl);
//
//        //게시물 엔티티 생성
//        Post post = dtoToEntity(postRequestDTO, findMember);
//        post.addFile(fileEntity);
//
//        //파일과 게시물 저장
//        fileRepository.save(fileEntity); //파일 먼저 저 (ID 필요시)
//        Post savePost = postRepository.save(post); //게시물 저장
//
//        return savePost.getId();
//    }
//
//    /**
//     * 게시물 저장 시 응답 API 처리 부분
//     */
//    @Transactional
//    public ResponseEntity<Map<String, Object>> createPost(PostRequestDTO postRequestDTO) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            Long post_id = savePost(postRequestDTO);
//            response.put("success", "true");
//            response.put("post_id", post_id);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            response.put("success", "false");
//            response.put("error", "작성 실패");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//
//    /**
//     * 글 상세 조회(단일) - PostCheck, PostCheckResponseDTO 사용
//     */
//    public PostCheckDTO findPostOne(Long post_id) {
//        Post findPost = postRepository.findById(post_id)
//                .orElseThrow(() -> new PostNotFoundException());
//
//        return convertToPostCheckDTO(findPost);
//    }
//
//    /**
//     * 글 목록 조회 - PostSummaryDTO, PostSummaryResponseDTO 사용
//     */
//    public PostSummaryResponseDTO getPosts(int page, int pageSize) {
//        PageRequest pageable = PageRequest.of(page, pageSize);
//        Page<Post> postPage = postRepository.findAll(pageable);
//        List<PostSummaryDTO> posts = postPage.getContent().stream()
//                .map(post -> new PostSummaryDTO(post.getId(), post.getTitle(), post.getCreated_at(),
//                        post.getMember() != null ? post.getMember().getId() : null)) // null 체크 포함
//                .toList();
//
//        return new PostSummaryResponseDTO(page, pageSize, postPage.getTotalElements(), postPage.getTotalPages(), posts);
//    }
//
//    /**
//     * 게시물 수정
//     */
//    @Transactional
//    public PostUpdateResponseDTO updatePost(Long postId, PostUpdateDTO postUpdateDTO) {
//
//        // 유효성 검사
//        if (postUpdateDTO.getTitle() == null || postUpdateDTO.getTitle().isEmpty()) {
//            throw new IllegalArgumentException("제목은 필수입니다.");
//        }
//        if (postUpdateDTO.getContent() == null || postUpdateDTO.getContent().isEmpty()) {
//            throw new IllegalArgumentException("내용은 필수입니다.");
//        }
//
//        Post findPost = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException());
//
//        findPost.update(postUpdateDTO.getTitle(), postUpdateDTO.getContent());
//
//        // 파일 업로드 및 URL 저장
//        List<MultipartFile> files = postUpdateDTO.getFiles();
//        if (files != null && !files.isEmpty()) {
//            List<File> existingFiles = findPost.getFiles();
//            for (File existingFile : existingFiles) {
//                s3ImageService.deleteImageFromS3(existingFile.getAccessUrl());
//                fileRepository.delete(existingFile);
//            }
//        }
//        findPost.getFiles().clear();
//
//        // 새로운 파일 업로드 및 저장
//        for (MultipartFile multipartFile : files) {
//            File file = new File(multipartFile.getOriginalFilename());
//            file.setFileSize(multipartFile.getSize());
//            file.setFileType(multipartFile.getContentType());
//            String imageUrl = s3ImageService.upload(multipartFile);
//            file.setAccessUrl(imageUrl);
//            file.setPost(findPost);
//            fileRepository.save(file);
//            findPost.addFile(file);
//
//        }
//        postRepository.save(findPost);
//        return convertToPostUpdateResponseDTO(findPost);
//}
//
//
///**
// * 글 삭제시 응답 API 처리
// */
//@Transactional
//public Map<String, Object> deletePost(Long post_id) {
//    Map<String, Object> response = new HashMap<>();
//    Post findPost = postRepository.findById(post_id)
//            .orElseThrow(() -> new PostNotFoundException());
//
//    postRepository.delete(findPost);
//
//    response.put("success", "true");
//    return response;
//}
//
//public static Post dtoToEntity(PostRequestDTO postRequestDTO, Member member) {
//    return Post.builder()
//            .title(postRequestDTO.getTitle())
//            .content(postRequestDTO.getContent())
//            .member(member)
//            .created_at(LocalDateTime.now()).build();
//}
//
//public PostCheckDTO convertToPostCheckDTO(Post findPost) {
//    List<FileDTO> fileDTOs = findPost.getFiles().stream()
//            .map(file -> new FileDTO(file.getId(), file.getOriginalFilename(), file.getAccessUrl()))
//            .collect(Collectors.toList());
//
//    return new PostCheckDTO(
//            findPost.getId(),
//            findPost.getTitle(),
//            findPost.getContent(),
//            findPost.getMember().getId(),
//            findPost.getCreated_at(),
//            fileDTOs //파일 정보 추가
//    );
//}
//
//public PostUpdateResponseDTO convertToPostUpdateResponseDTO(Post findPost) {
//    return new PostUpdateResponseDTO(
//            findPost.getId(),
//            findPost.getTitle(),
//            findPost.getContent(),
//            findPost.getMember().getId(),
//            findPost.getUpdate_at()
//    );
//}
}
