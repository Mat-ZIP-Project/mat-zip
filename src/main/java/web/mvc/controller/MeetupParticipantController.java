package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.mvc.domain.MeetupParticipant;
import web.mvc.dto.MeetupParticipantRequestDto;
import web.mvc.dto.MeetupParticipantResponseDto;
import web.mvc.service.MeetupParticipantService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meetup-participants")
@RequiredArgsConstructor
public class MeetupParticipantController {

    private final MeetupParticipantService meetupParticipantService;

    // 1. 모임 참가 신청
    @PostMapping
    public ResponseEntity<MeetupParticipantResponseDto> joinMeeting(@RequestBody MeetupParticipantRequestDto dto) {
        MeetupParticipant participant = meetupParticipantService.joinMeeting(dto);
        return ResponseEntity.ok(MeetupParticipantResponseDto.fromEntity(participant));
    }

    // 2. 모임 참가 취소
    @DeleteMapping("/{joinId}")
    public ResponseEntity<Void> cancelJoin(@PathVariable Long joinId) {
        meetupParticipantService.cancelJoin(joinId);
        return ResponseEntity.noContent().build();
    }

    // 3. 특정 모임 참가자 목록 조회
    @GetMapping("/meeting/{meetingId}")
    public ResponseEntity<List<MeetupParticipantResponseDto>> getParticipantsByMeeting(@PathVariable Long meetingId) {
        List<MeetupParticipant> participants = meetupParticipantService.getParticipantsByMeeting(meetingId);
        List<MeetupParticipantResponseDto> result = participants.stream()
                .map(MeetupParticipantResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // 4. 특정 회원 참가 모임 목록 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MeetupParticipantResponseDto>> getMyJoinedMeetings(@PathVariable Long userId) {
        List<MeetupParticipant> myParticipations = meetupParticipantService.getMyJoinedMeetings(userId);
        List<MeetupParticipantResponseDto> result = myParticipations.stream()
                .map(MeetupParticipantResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
