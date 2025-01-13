import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FinalDetailsComponent } from './final-details.component';

describe('FinalDetailsComponent', () => {
  let component: FinalDetailsComponent;
  let fixture: ComponentFixture<FinalDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FinalDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FinalDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
