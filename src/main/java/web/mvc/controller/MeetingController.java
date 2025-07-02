package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.mvc.domain.Meeting;
import web.mvc.dto.MeetingRequestDto;
import web.mvc.dto.MeetingResponseDto;
import web.mvc.service.MeetingService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    // 1. 모임 생성
    @PostMapping
    public ResponseEntity<MeetingResponseDto> createMeeting(@RequestBody MeetingRequestDto requestDto) {
        Meeting meeting = meetingService.createMeeting(requestDto);
        return ResponseEntity.ok(MeetingResponseDto.fromEntity(meeting));
    }

    // 2. 전체 모임 목록 조회
    @GetMapping
    public ResponseEntity<List<MeetingResponseDto>> getAllMeetings() {
        List<Meeting> meetings = meetingService.getAllMeetings();
        List<MeetingResponseDto> result = meetings.stream()
                .map(MeetingResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // 3. 특정 모임 상세 조회
    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingResponseDto> getMeetingById(@PathVariable Long meetingId) {
        Meeting meeting = meetingService.getMeetingById(meetingId);
        return ResponseEntity.ok(MeetingResponseDto.fromEntity(meeting));
    }

    // 4. 모임 수정
    @PutMapping("/{meetingId}")
    public ResponseEntity<MeetingResponseDto> updateMeeting(@PathVariable Long meetingId, @RequestBody MeetingRequestDto requestDto) {
        Meeting updated = meetingService.updateMeeting(meetingId, requestDto);
        return ResponseEntity.ok(MeetingResponseDto.fromEntity(updated));
    }

    // 5. 모임 삭제
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable Long meetingId) {
        meetingService.deleteMeeting(meetingId);
        return ResponseEntity.noContent().build();
    }
}
