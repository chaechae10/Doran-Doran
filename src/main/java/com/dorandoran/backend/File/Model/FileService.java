package com.dorandoran.backend.File.Model;

import com.dorandoran.backend.File.exception.CustomS3Exception;
import com.dorandoran.backend.File.exception.ErrorCode;
import com.dorandoran.backend.File.exception.FileMissingException;
import com.dorandoran.backend.Marker.Model.Marker;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@Getter
public class FileService {

    private final FileRepository fileRepository;
    private final S3ImageService s3ImageService;

    //파일 생성
    public File createFile(MultipartFile image) {
        validateImage(image);
        String imageUrl = s3ImageService.upload(image);
        File file = new File(image.getOriginalFilename());
        file.setStoreFilename(generateFileName(image.getOriginalFilename()));
        file.setAccessUrl(imageUrl);
        return fileRepository.save(file);
    }

    //파일 아이디로 조회
    public File getFileById(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new FileMissingException("파일이 존재하지 않습니다. 파일 아이디 : " + id));
    }

    // 파일 수정
    public File updateFile(Long id, String newFileName, MultipartFile newFile) {
        File findFile = getFileById(id);

        //S3에서 기존 파일 삭제
        s3ImageService.deleteImageFromS3(findFile.getAccessUrl());

        // 새로운 파일을 S3에 업로드하고 URL을 가져옴
        String newImageUrl = s3ImageService.upload(newFile);

        // 파일 정보 업데이트
        findFile.setFileName(newFileName); // 새로운 파일 이름으로 설정
        findFile.setAccessUrl(newImageUrl); // S3 URL 업데이트
        return fileRepository.save(findFile); // 변경된 파일 저장
    }

    //파일 삭제
    public void deleteFile(Long id) {
        File findFile = getFileById(id); // 파일 조회
        // S3에서 파일 삭제 (필요한 경우)
        s3ImageService.deleteImageFromS3(findFile.getS3Url());
        // 데이터베이스에서 파일 삭제
        fileRepository.delete(findFile);
    }

    //파일 검증
    public void validateImage(MultipartFile image) {

        //파일 유효성 검사
        if(image==null || image.isEmpty()) {
            throw new CustomS3Exception(ErrorCode.EMPTY_FILE_EXCEPTION, "업로드된 파일이 없습니다.");
        }
        //파일 크기 체크
        if(image.getSize()>5*1024*1024) {
            throw new CustomS3Exception(ErrorCode.FILE_SIZE_EXCEPTION, "파일 크기는 5MB를 초과할 수 없습니다.");
        }
        //파일 형식 체크
        List<String> allowTypes = List.of("image/jpeg", "image/png", "image/jpg");
        if(!allowTypes.contains(image.getContentType())) {
            throw new CustomS3Exception(ErrorCode.INVALID_FILE_TYPE_EXCEPTION, "허용되지 않는 파일 형식입니다.");
        }
    }



    private String generateFileName(String originalFilename) {
        return UUID.randomUUID() + extractExtension(originalFilename); // 파일 이름 생성
    }


    private String extractExtension(String originalFilename) {
        int index = originalFilename.lastIndexOf('.');
        return (index == -1) ? "" : originalFilename.substring(index); // 확장자 추출 또는 빈 문자열 반환
    }

    //파일 생성 후 마커와 연결하는 메서드 (File 기본 생성자 protected)
    public File createFile(String imageUrl, Marker marker) {
        File file = new File();
        file.setAccessUrl(imageUrl);
        file.assignMarker(marker);
        fileRepository.save(file);
        return file;
    }
}
